package org.xiyu.reforged.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.xiyu.reforged.asm.BytecodeRewriter;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NeoMixinExtractor — extracts a NeoForge mod's own Mixin infrastructure so it
 * can be applied by Forge's Mixin environment.
 *
 * <p>NeoForge mods declare mixin configs in {@code neoforge.mods.toml}
 * ({@code [[mixins]]} blocks). ReForged's discovery placeholder jar is loaded
 * by Forge onto the game layer (TRANSFORMER classloader), which is the only
 * place where Sponge Mixin can patch vanilla classes. This extractor therefore
 * collects, for each mixin config:</p>
 *
 * <ul>
 *   <li>the config JSON itself (with {@code required} relaxed and a missing
 *       refmap reference stripped),</li>
 *   <li>the mixin classes plus the <b>transitive closure</b> of mod classes
 *       they reference (interfaces they implement, helper classes invoked from
 *       injected method bodies, the mixin plugin, …), rewritten through
 *       {@link BytecodeRewriter} so NeoForge references resolve on Forge,</li>
 *   <li>a {@code META-INF/reforged-parent-classes.txt} manifest listing every
 *       kept class, which {@code NeoModClassLoader} reads at runtime to force
 *       parent-first delegation — guaranteeing a single class identity for
 *       types shared between the TRANSFORMER loader and the NeoMod loader
 *       (e.g. duck interfaces such as {@code ICustomKnockback} that mixins
 *       attach to vanilla classes).</li>
 * </ul>
 */
public final class NeoMixinExtractor {

    /** Resource name (inside placeholder jars) listing parent-first class names. */
    public static final String PARENT_CLASSES_ENTRY = "META-INF/reforged-parent-classes.txt";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern MIXIN_CONFIG_PATTERN =
            Pattern.compile("(?m)^\\s*config\\s*=\\s*\"([^\"]+)\"");

    /** Hard cap so a pathological reference graph cannot balloon the placeholder. */
    private static final int MAX_CLOSURE_SIZE = 4000;

    /**
     * Extraction result.
     *
     * @param classFiles    entryName ({@code a/b/C.class}) → rewritten bytes
     * @param mixinConfigs  entryName ({@code x.mixins.json}) → rewritten JSON
     * @param extraResources entryName → raw bytes (refmaps, parent-class list)
     * @param configNames   mixin config file names for the {@code MixinConfigs} manifest attribute
     */
    public record Result(Map<String, byte[]> classFiles,
                         Map<String, String> mixinConfigs,
                         Map<String, byte[]> extraResources,
                         List<String> configNames) {

        public boolean isEmpty() {
            return configNames.isEmpty();
        }
    }

    private interface Log {
        void accept(String message);
    }

    private NeoMixinExtractor() {}

    /**
     * Extract mixin payload from a NeoForge mod jar.
     *
     * @param jar        the original (unpatched) NeoForge jar
     * @param neoToml    content of {@code META-INF/neoforge.mods.toml}
     * @param logSink    receives human-readable progress/warning lines
     */
    public static Result extract(JarFile jar, String neoToml, java.util.function.Consumer<String> logSink) {
        Log log = logSink::accept;
        List<String> configNames = findMixinConfigNames(neoToml, jar);
        if (configNames.isEmpty()) {
            return new Result(Map.of(), Map.of(), Map.of(), List.of());
        }

        // Index all class entries in the jar: internalName -> bytes.
        // Jar-in-Jar libraries are indexed too (recursively) so the reference
        // closure can follow mod → library edges; otherwise TRANSFORMER-side
        // resolution of pinned classes whose signatures touch JiJ types fails.
        Map<String, byte[]> jarClasses = new HashMap<>();
        var entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class") && !entry.isDirectory() && !name.contains("META-INF/")) {
                String internal = name.substring(0, name.length() - ".class".length());
                jarClasses.put(internal, readEntry(jar, entry));
            } else if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar") && !entry.isDirectory()) {
                indexNestedJarClasses(readEntry(jar, entry), jarClasses, 0);
            }
        }

        Map<String, String> rewrittenConfigs = new LinkedHashMap<>();
        Map<String, byte[]> extraResources = new LinkedHashMap<>();
        List<String> keptConfigNames = new ArrayList<>();
        Set<String> seedClasses = new HashSet<>();   // internal names

        for (String configName : configNames) {
            JarEntry configEntry = jar.getJarEntry(configName);
            if (configEntry == null) {
                log.accept("Mixin config " + configName + " declared but missing from jar — skipped");
                continue;
            }
            String json = new String(readEntry(jar, configEntry), StandardCharsets.UTF_8);
            try {
                JsonObject config = JsonParser.parseString(json).getAsJsonObject();
                String pkg = config.has("package") ? config.get("package").getAsString() : "";
                String pkgInternal = pkg.replace('.', '/');

                for (String listKey : new String[]{"mixins", "client", "server"}) {
                    if (!config.has(listKey)) continue;
                    JsonArray arr = config.getAsJsonArray(listKey);
                    for (JsonElement el : arr) {
                        String cls = pkgInternal + "/" + el.getAsString().replace('.', '/');
                        if (jarClasses.containsKey(cls)) {
                            seedClasses.add(cls);
                        } else {
                            log.accept("Mixin class " + cls + " not found in jar (config " + configName + ")");
                        }
                    }
                }
                if (config.has("plugin")) {
                    String plugin = config.get("plugin").getAsString().replace('.', '/');
                    if (jarClasses.containsKey(plugin)) {
                        seedClasses.add(plugin);
                    }
                }

                // Soften failure handling: a NeoForge mixin that cannot apply on
                // Forge should not hard-crash the whole game. defaultRequire=0
                // additionally downgrades injector failures (e.g. targets that
                // only exist in NeoForge-patched vanilla code) to debug logs.
                config.addProperty("required", false);
                JsonObject injectors = config.has("injectors")
                        ? config.getAsJsonObject("injectors") : new JsonObject();
                injectors.addProperty("defaultRequire", 0);
                config.add("injectors", injectors);

                // Keep the refmap only if it actually exists in the jar.
                if (config.has("refmap")) {
                    String refmap = config.get("refmap").getAsString();
                    JarEntry refmapEntry = jar.getJarEntry(refmap);
                    if (refmapEntry != null) {
                        extraResources.put(refmap, readEntry(jar, refmapEntry));
                    } else {
                        config.remove("refmap");
                    }
                }

                rewrittenConfigs.put(configName, GSON.toJson(config));
                keptConfigNames.add(configName);
            } catch (Exception e) {
                log.accept("Failed to parse mixin config " + configName + ": " + e.getMessage());
            }
        }

        if (seedClasses.isEmpty()) {
            return new Result(Map.of(), Map.of(), Map.of(), List.of());
        }

        // ServiceLoader implementations must also live in the placeholder:
        // closure classes initializing on the TRANSFORMER loader frequently
        // bootstrap through ServiceLoader (e.g. GeckoLib's platform services).
        entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith("META-INF/services/") || entry.isDirectory()) continue;
            String serviceInterface = name.substring("META-INF/services/".length());
            if (serviceInterface.startsWith("cpw.mods.") || serviceInterface.startsWith("org.spongepowered.")
                    || serviceInterface.startsWith("net.minecraftforge.") || serviceInterface.startsWith("javax.")) {
                continue;
            }
            String content = new String(readEntry(jar, entry), StandardCharsets.UTF_8);
            for (String line : content.split("\\R")) {
                String impl = line.trim();
                int comment = impl.indexOf('#');
                if (comment >= 0) impl = impl.substring(0, comment).trim();
                if (impl.isEmpty()) continue;
                String internal = impl.replace('.', '/');
                if (jarClasses.containsKey(internal)) {
                    seedClasses.add(internal);
                }
            }
        }

        // Transitive closure over jar-internal class references.
        Set<String> closure = new TreeSet<>();
        Deque<String> queue = new ArrayDeque<>(seedClasses);
        while (!queue.isEmpty() && closure.size() < MAX_CLOSURE_SIZE) {
            String current = queue.poll();
            if (!closure.add(current)) continue;
            byte[] bytes = jarClasses.get(current);
            if (bytes == null) continue;
            for (String ref : collectReferences(bytes)) {
                if (jarClasses.containsKey(ref) && !closure.contains(ref)) {
                    queue.add(ref);
                }
            }
        }
        if (closure.size() >= MAX_CLOSURE_SIZE) {
            log.accept("Mixin closure hit cap of " + MAX_CLOSURE_SIZE + " classes — placeholder may be incomplete");
        }

        // Rewrite all closure classes for the Forge classpath.
        BytecodeRewriter rewriter = new BytecodeRewriter();
        Map<String, byte[]> classFiles = new LinkedHashMap<>();
        Map<String, byte[]> rewrittenByInternal = new HashMap<>();
        for (String internal : closure) {
            byte[] original = jarClasses.get(internal);
            byte[] rewritten = rewriter.rewrite(original);
            // Mixin classes themselves are consumed by the Mixin processor as
            // ClassNodes (members are copied into targets) and must keep their
            // members private per Mixin's validation rules. Everything else is
            // widened to public for cross-loader access (see below).
            if (!seedClasses.contains(internal)) {
                rewritten = normalizeForPlaceholder(rewritten);
            }
            classFiles.put(internal + ".class", rewritten);
            rewrittenByInternal.put(internal, rewritten);
        }

        // Pin parent-first (single identity across TRANSFORMER/NeoMod loaders):
        //  1. all closure INTERFACES — duck interfaces attached to vanilla
        //     classes must match at cast sites;
        //  2. classes directly referenced from mixin class bodies — the injected
        //     code shares static state (configs, counters, queues) with the rest
        //     of the mod, so both sides must see the same Class;
        //  3. the transitive SIGNATURE closure of the above — the JVM registers
        //     loader constraints on types appearing in super types and
        //     field/method descriptors when hierarchies span both loaders.
        // @Mod entrypoint classes are excluded: they are instantiated by
        // ReForged on the child loader and rely on child-side semantics
        // (extracted-directory resources). Mixin seed classes themselves are
        // never loaded normally and need no pin.
        Set<String> pinSeeds = new HashSet<>();
        for (String internal : closure) {
            byte[] rewritten = rewrittenByInternal.get(internal);
            if (rewritten != null && isInterface(rewritten)) {
                pinSeeds.add(internal);
            }
        }
        for (String mixinClass : seedClasses) {
            byte[] bytes = rewrittenByInternal.get(mixinClass);
            if (bytes == null) continue;
            for (String ref : collectReferences(bytes)) {
                if (rewrittenByInternal.containsKey(ref) && !seedClasses.contains(ref)) {
                    pinSeeds.add(ref);
                }
            }
        }

        // Group classes by top-level (nest host) name: nestmates must always be
        // pinned together, otherwise private member access across the nest
        // fails JVM nest verification when identities split between loaders.
        Map<String, List<String>> nestGroups = new HashMap<>();
        for (String internal : rewrittenByInternal.keySet()) {
            nestGroups.computeIfAbsent(topLevelName(internal), k -> new ArrayList<>()).add(internal);
        }

        Set<String> pinned = expandPins(pinSeeds, rewrittenByInternal, nestGroups);

        // Pinned classes execute on the TRANSFORMER loader, so any class their
        // METHOD BODIES touch (config holders, helpers with static state) must
        // share that identity too — otherwise the child loader fills one copy
        // while injected code reads another (e.g. VehicleConfig fields staying
        // null on the TRANSFORMER side). Expand by one body level, then close
        // over signatures/nests again.
        Set<String> bodyExpansion = new HashSet<>(pinned);
        for (String internal : pinned) {
            byte[] bytes = rewrittenByInternal.get(internal);
            if (bytes == null) continue;
            for (String ref : collectReferences(bytes)) {
                if (rewrittenByInternal.containsKey(ref)) {
                    bodyExpansion.add(ref);
                }
            }
        }
        pinned = expandPins(bodyExpansion, rewrittenByInternal, nestGroups);

        // Exclude @Mod entrypoint nest groups as a whole (nestmates must share
        // a loader), plus the mixin seed classes themselves.
        Set<String> excludedTopLevels = new HashSet<>();
        for (String internal : pinned) {
            byte[] bytes = rewrittenByInternal.get(internal);
            if (bytes != null && isModEntrypoint(bytes)) {
                excludedTopLevels.add(topLevelName(internal));
            }
        }
        Set<String> parentFirstNames = new TreeSet<>();
        for (String internal : pinned) {
            if (excludedTopLevels.contains(topLevelName(internal))) continue;
            if (seedClasses.contains(internal)) continue;
            parentFirstNames.add(internal.replace('/', '.'));
        }

        extraResources.put(PARENT_CLASSES_ENTRY,
                String.join("\n", parentFirstNames).getBytes(StandardCharsets.UTF_8));

        log.accept("Mixin payload: " + keptConfigNames.size() + " config(s), "
                + seedClasses.size() + " mixin class(es), closure " + closure.size()
                + " class(es), pinned " + parentFirstNames.size());

        return new Result(classFiles, rewrittenConfigs, extraResources, keptConfigNames);
    }

    /** Find declared mixin config names from neoforge.mods.toml plus conventional fallbacks. */
    private static List<String> findMixinConfigNames(String neoToml, JarFile jar) {
        Set<String> names = new java.util.LinkedHashSet<>();
        if (neoToml != null) {
            Matcher m = MIXIN_CONFIG_PATTERN.matcher(neoToml);
            while (m.find()) {
                names.add(m.group(1));
            }
        }
        // Some mods rely on MixinConfigs manifest attribute even on NeoForge.
        try {
            var manifest = jar.getManifest();
            if (manifest != null) {
                String attr = manifest.getMainAttributes().getValue("MixinConfigs");
                if (attr != null && !attr.isBlank()) {
                    for (String part : attr.split(",")) {
                        if (!part.isBlank()) names.add(part.trim());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(names);
    }

    /** Strip inner-class suffixes: {@code a/b/C$D$1} → {@code a/b/C}. */
    private static String topLevelName(String internalName) {
        int dollar = internalName.indexOf('$');
        return dollar == -1 ? internalName : internalName.substring(0, dollar);
    }

    /**
     * Close a pin seed set over signature references and nest groups: every
     * pinned class pulls in its nestmates and the types appearing in its
     * super types / field / method descriptors.
     */
    private static Set<String> expandPins(Set<String> seeds, Map<String, byte[]> classes,
                                          Map<String, List<String>> nestGroups) {
        Set<String> pinned = new TreeSet<>();
        Deque<String> queue = new ArrayDeque<>(seeds);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!pinned.add(current)) continue;
            for (String nestmate : nestGroups.getOrDefault(topLevelName(current), List.of())) {
                if (!pinned.contains(nestmate)) {
                    queue.add(nestmate);
                }
            }
            byte[] bytes = classes.get(current);
            if (bytes == null) continue;
            for (String ref : collectSignatureReferences(bytes)) {
                if (classes.containsKey(ref) && !pinned.contains(ref)) {
                    queue.add(ref);
                }
            }
        }
        return pinned;
    }

    /** Index classes of a nested Jar-in-Jar archive (recursively, bounded depth). */
    private static void indexNestedJarClasses(byte[] jarBytes, Map<String, byte[]> jarClasses, int depth) {
        if (depth > 5 || jarBytes.length == 0) return;
        try {
            java.nio.file.Path temp = java.nio.file.Files.createTempFile("reforged-mixin-jij-", ".jar");
            try {
                java.nio.file.Files.write(temp, jarBytes);
                try (JarFile nested = new JarFile(temp.toFile())) {
                    var nestedEntries = nested.entries();
                    while (nestedEntries.hasMoreElements()) {
                        JarEntry entry = nestedEntries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class") && !entry.isDirectory() && !name.contains("META-INF/")) {
                            String internal = name.substring(0, name.length() - ".class".length());
                            jarClasses.putIfAbsent(internal, readEntry(nested, entry));
                        } else if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar") && !entry.isDirectory()) {
                            indexNestedJarClasses(readEntry(nested, entry), jarClasses, depth + 1);
                        }
                    }
                }
            } finally {
                java.nio.file.Files.deleteIfExists(temp);
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isInterface(byte[] classBytes) {
        try {
            return (new ClassReader(classBytes).getAccess() & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** True if the class carries a {@code @Mod} annotation (NeoForge or remapped Forge form). */
    private static boolean isModEntrypoint(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (var annotations : new List[]{node.visibleAnnotations, node.invisibleAnnotations}) {
                if (annotations == null) continue;
                for (Object a : annotations) {
                    String desc = ((org.objectweb.asm.tree.AnnotationNode) a).desc;
                    if ("Lnet/minecraftforge/fml/common/Mod;".equals(desc)
                            || "Lnet/neoforged/fml/common/Mod;".equals(desc)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Collect classes referenced from a class's "shape": super types and
     * field/method descriptors (including thrown exceptions). Method bodies are
     * skipped — this is the set the JVM enforces loader constraints on.
     */
    private static Set<String> collectSignatureReferences(byte[] classBytes) {
        Set<String> refs = new HashSet<>();
        try {
            ClassReader reader = new ClassReader(classBytes);
            reader.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature,
                                  String superName, String[] interfaces) {
                    if (superName != null) refs.add(superName);
                    if (interfaces != null) java.util.Collections.addAll(refs, interfaces);
                }

                @Override
                public org.objectweb.asm.FieldVisitor visitField(int access, String name, String descriptor,
                                                                  String signature, Object value) {
                    addType(org.objectweb.asm.Type.getType(descriptor));
                    return null;
                }

                @Override
                public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String descriptor,
                                                                    String signature, String[] exceptions) {
                    for (org.objectweb.asm.Type arg : org.objectweb.asm.Type.getArgumentTypes(descriptor)) {
                        addType(arg);
                    }
                    addType(org.objectweb.asm.Type.getReturnType(descriptor));
                    if (exceptions != null) java.util.Collections.addAll(refs, exceptions);
                    return null;
                }

                private void addType(org.objectweb.asm.Type type) {
                    while (type.getSort() == org.objectweb.asm.Type.ARRAY) {
                        type = type.getElementType();
                    }
                    if (type.getSort() == org.objectweb.asm.Type.OBJECT) {
                        refs.add(type.getInternalName());
                    }
                }
            }, ClassReader.SKIP_CODE);
        } catch (Exception ignored) {
        }
        return refs;
    }

    /**
     * Normalize a placeholder (TRANSFORMER-side) class copy:
     *
     * <ul>
     *   <li>Widen the class and all members to {@code public}. Placeholder
     *       copies share packages with child-loader copies, but the JVM treats
     *       same-named packages on different loaders as distinct runtime
     *       packages, so package-private/protected access across the split
     *       fails ({@code IllegalAccessError}); private nest-member access
     *       fails nest verification. Public access bypasses both checks.
     *       (Forge's {@code EventAccessTransformer} also hard-fails on private
     *       {@code @SubscribeEvent} members — covered by the same widening.)</li>
     * </ul>
     */
    private static byte[] normalizeForPlaceholder(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            node.access = widenAccess(node.access);
            if (node.innerClasses != null) {
                for (var inner : node.innerClasses) {
                    inner.access = widenAccess(inner.access);
                }
            }
            for (var field : node.fields) {
                field.access = widenAccess(field.access);
            }
            for (var method : node.methods) {
                method.access = widenAccess(method.access);
            }

            org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Exception e) {
            return classBytes;
        }
    }

    private static int widenAccess(int access) {
        return (access & ~(org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_PROTECTED))
                | org.objectweb.asm.Opcodes.ACC_PUBLIC;
    }

    /** Collect every class referenced from a class file (descriptors, signatures, annotations, frames). */
    private static Set<String> collectReferences(byte[] classBytes) {
        Set<String> refs = new HashSet<>();
        try {
            Remapper collector = new Remapper() {
                @Override
                public String map(String internalName) {
                    refs.add(internalName);
                    return internalName;
                }
            };
            ClassReader reader = new ClassReader(classBytes);
            reader.accept(new ClassRemapper(new ClassNode(), collector), 0);
        } catch (Exception ignored) {
            // Unparseable class — no references collected.
        }
        return refs;
    }

    private static byte[] readEntry(JarFile jar, JarEntry entry) {
        try (InputStream is = jar.getInputStream(entry)) {
            return is.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
