package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * NeoForgeModAnalyzer — Pre-load diagnostic scanner for NeoForge mods.
 *
 * <p>Scans NeoForge mod JARs <b>before</b> they are loaded, and generates a
 * compatibility report showing:</p>
 * <ul>
 *   <li>Which NeoForge API packages/classes each mod references</li>
 *   <li>Which APIs are covered by ReForged shims vs. missing</li>
 *   <li>Class count, annotation usage, and constructor patterns</li>
 *   <li>Potential compatibility risks and recommendations</li>
 * </ul>
 *
 * <p>This is especially useful during development to quickly assess whether
 * a NeoForge test mod added via {@code build.gradle} will work under ReForged.</p>
 */
public final class NeoForgeModAnalyzer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    //  Known NeoForge API categories and their ReForged coverage
    // ═══════════════════════════════════════════════════════════

    /** API categories → package prefixes that belong to that category. */
    private static final Map<String, List<String>> API_CATEGORIES = new LinkedHashMap<>();

    /** Package prefixes that are shimmed/proxied by ReForged. */
    private static final Set<String> SHIMMED_PACKAGES = new LinkedHashSet<>();

    static {
        // ── Category definitions ──────────────────────────────────
        API_CATEGORIES.put("Events (Bus API)", List.of(
                "net/neoforged/bus/api/",
                "net/neoforged/neoforge/event/",
                "net/neoforged/neoforge/client/event/"
        ));
        API_CATEGORIES.put("FML (Mod Loading)", List.of(
                "net/neoforged/fml/",
                "net/neoforged/fml/common/",
                "net/neoforged/fml/config/",
                "net/neoforged/fml/event/",
                "net/neoforged/fml/loading/"
        ));
        API_CATEGORIES.put("Registries", List.of(
                "net/neoforged/neoforge/registries/",
                "net/neoforged/neoforge/registries/datamaps/",
                "net/neoforged/neoforge/registries/holdersets/"
        ));
        API_CATEGORIES.put("Capabilities", List.of(
                "net/neoforged/neoforge/capabilities/"
        ));
        API_CATEGORIES.put("Data Attachments", List.of(
                "net/neoforged/neoforge/attachment/"
        ));
        API_CATEGORIES.put("Network", List.of(
                "net/neoforged/neoforge/network/"
        ));
        API_CATEGORIES.put("Client Extensions", List.of(
                "net/neoforged/neoforge/client/",
                "net/neoforged/neoforge/client/extensions/",
                "net/neoforged/neoforge/client/model/"
        ));
        API_CATEGORIES.put("Common Extensions", List.of(
                "net/neoforged/neoforge/common/",
                "net/neoforged/neoforge/common/extensions/",
                "net/neoforged/neoforge/common/util/"
        ));
        API_CATEGORIES.put("Fluids", List.of(
                "net/neoforged/neoforge/fluids/"
        ));
        API_CATEGORIES.put("Items / Crafting", List.of(
                "net/neoforged/neoforge/common/crafting/",
                "net/neoforged/neoforge/items/"
        ));
        API_CATEGORIES.put("Dist / API Marker", List.of(
                "net/neoforged/api/distmarker/",
                "net/neoforged/api/"
        ));
        API_CATEGORIES.put("NeoForge SPI", List.of(
                "net/neoforged/neoforgespi/",
                "net/neoforged/fml/loading/modscan/"
        ));

        // ── ReForged shimmed packages (present in our proxy/shim layer) ──
        // These correspond to packages that ReForged has proxy/shim classes for
        SHIMMED_PACKAGES.addAll(List.of(
                "net/neoforged/bus/api/",
                "net/neoforged/fml/",
                "net/neoforged/fml/common/",
                "net/neoforged/fml/config/",
                "net/neoforged/fml/event/",
                "net/neoforged/fml/event/lifecycle/",
                "net/neoforged/fml/loading/",
                "net/neoforged/fml/loading/modscan/",
                "net/neoforged/api/distmarker/",
                "net/neoforged/api/",
                "net/neoforged/neoforge/event/",
                "net/neoforged/neoforge/event/entity/",
                "net/neoforged/neoforge/event/entity/living/",
                "net/neoforged/neoforge/event/entity/player/",
                "net/neoforged/neoforge/event/level/",
                "net/neoforged/neoforge/event/tick/",
                "net/neoforged/neoforge/event/server/",
                "net/neoforged/neoforge/client/event/",
                "net/neoforged/neoforge/registries/",
                "net/neoforged/neoforge/registries/datamaps/",
                "net/neoforged/neoforge/registries/holdersets/",
                "net/neoforged/neoforge/registries/callback/",
                "net/neoforged/neoforge/capabilities/",
                "net/neoforged/neoforge/attachment/",
                "net/neoforged/neoforge/network/",
                "net/neoforged/neoforge/network/handling/",
                "net/neoforged/neoforge/network/configuration/",
                "net/neoforged/neoforge/network/connection/",
                "net/neoforged/neoforge/network/event/",
                "net/neoforged/neoforge/network/codec/",
                "net/neoforged/neoforge/network/payload/",
                "net/neoforged/neoforge/network/registration/",
                "net/neoforged/neoforge/client/",
                "net/neoforged/neoforge/client/extensions/",
                "net/neoforged/neoforge/client/model/",
                "net/neoforged/neoforge/common/",
                "net/neoforged/neoforge/common/extensions/",
                "net/neoforged/neoforge/common/util/",
                "net/neoforged/neoforge/common/util/strategy/",
                "net/neoforged/neoforge/common/crafting/",
                "net/neoforged/neoforge/fluids/",
                "net/neoforged/neoforge/items/",
                "net/neoforged/neoforgespi/",
                "net/neoforged/neoforgespi/language/"
        ));
    }

    // ═══════════════════════════════════════════════════════════
    //  Analysis result
    // ═══════════════════════════════════════════════════════════

    /** Result of analyzing a single NeoForge mod JAR. */
    public record ModAnalysis(
            String jarName,
            String modId,
            int totalClasses,
            int totalNeoRefs,
            Map<String, Set<String>> apiCategoryToClasses,
            Set<String> shimmedRefs,
            Set<String> unshimmedRefs,
            Set<String> constructorPatterns,
            Set<String> annotations,
            List<String> warnings,
            double compatibilityScore
    ) {
        /** Whether this mod is likely to work under ReForged. */
        public String compatibilityRating() {
            if (compatibilityScore >= 0.90) return "HIGH";
            if (compatibilityScore >= 0.60) return "MEDIUM";
            if (compatibilityScore >= 0.30) return "LOW";
            return "VERY LOW";
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════

    /**
     * Analyze all NeoForge mod JARs and log a comprehensive compatibility report.
     *
     * @param jars list of NeoForge mod JAR paths
     */
    public static void analyzeAndReport(List<Path> jars) {
        if (jars.isEmpty()) return;

        LOGGER.info("[ReForged] ╔══════════════════════════════════════════════════╗");
        LOGGER.info("[ReForged] ║    NeoForge Mod Compatibility Analysis Report    ║");
        LOGGER.info("[ReForged] ╚══════════════════════════════════════════════════╝");

        List<ModAnalysis> analyses = new ArrayList<>();
        for (Path jar : jars) {
            try {
                ModAnalysis analysis = analyzeJar(jar);
                analyses.add(analysis);
                logModAnalysis(analysis);
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to analyze {}: {}", jar.getFileName(), e.getMessage());
            }
        }

        // Summary
        if (analyses.size() > 1) {
            logSummary(analyses);
        }
    }

    /**
     * Analyze a single NeoForge mod JAR.
     *
     * @param jarPath path to the JAR file
     * @return analysis result
     */
    public static ModAnalysis analyzeJar(Path jarPath) throws Exception {
        String jarName = jarPath.getFileName().toString();
        DependencyScanner scanner = new DependencyScanner();
        String detectedModId = null;
        int classCount = 0;

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                classCount++;
                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    reader.accept(scanner, ClassReader.SKIP_FRAMES);
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }

            // Try to get modId from TOML
            detectedModId = extractModId(jar);
        }

        // Categorize references
        Map<String, Set<String>> categoryToClasses = new LinkedHashMap<>();
        Set<String> shimmed = new TreeSet<>();
        Set<String> unshimmed = new TreeSet<>();

        for (String ref : scanner.neoForgeRefs) {
            // Categorize
            for (var entry : API_CATEGORIES.entrySet()) {
                for (String prefix : entry.getValue()) {
                    if (ref.startsWith(prefix)) {
                        categoryToClasses.computeIfAbsent(entry.getKey(), k -> new TreeSet<>()).add(ref);
                        break;
                    }
                }
            }

            // Check shim coverage
            String pkg = ref.contains("/") ? ref.substring(0, ref.lastIndexOf('/') + 1) : "";
            boolean isShimmed = SHIMMED_PACKAGES.stream().anyMatch(pkg::startsWith);
            if (isShimmed) {
                shimmed.add(ref);
            } else {
                unshimmed.add(ref);
            }
        }

        // Generate warnings
        List<String> warnings = new ArrayList<>();
        if (!unshimmed.isEmpty()) {
            // Group unshimmed by package
            Map<String, List<String>> byPkg = unshimmed.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.contains("/") ? r.substring(0, r.lastIndexOf('/')) : "(default)",
                            Collectors.toList()));
            for (var entry : byPkg.entrySet()) {
                warnings.add("Unshimmed package: " + entry.getKey().replace('/', '.') +
                        " (" + entry.getValue().size() + " class refs)");
            }
        }

        if (scanner.usesServiceLoader) {
            warnings.add("Uses java.util.ServiceLoader — may need service file remapping");
        }
        if (scanner.usesReflection) {
            warnings.add("Uses reflection (Class.forName/getDeclaredMethod) — dynamic access may fail");
        }
        if (scanner.usesMixin) {
            warnings.add("Contains Mixin classes — mixin targets may differ between NeoForge and Forge");
        }
        if (scanner.usesAccessTransformer) {
            warnings.add("Contains access transformer config — AT targets may differ");
        }

        // Calculate compatibility score
        int totalRefs = scanner.neoForgeRefs.size();
        double score = totalRefs == 0 ? 1.0 : (double) shimmed.size() / totalRefs;

        return new ModAnalysis(
                jarName,
                detectedModId != null ? detectedModId : "(unknown)",
                classCount,
                totalRefs,
                categoryToClasses,
                shimmed,
                unshimmed,
                scanner.constructorPatterns,
                scanner.annotations,
                warnings,
                score
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  Logging
    // ═══════════════════════════════════════════════════════════

    private static void logModAnalysis(ModAnalysis a) {
        LOGGER.info("[ReForged] ┌──────────────────────────────────────────────────");
        LOGGER.info("[ReForged] │ Mod: {} ({})", a.modId(), a.jarName());
        LOGGER.info("[ReForged] │ Classes: {} | NeoForge API refs: {} | Compatibility: {} ({})",
                a.totalClasses(), a.totalNeoRefs(),
                a.compatibilityRating(), String.format("%.0f%%", a.compatibilityScore() * 100));
        LOGGER.info("[ReForged] ├──────────────────────────────────────────────────");

        // API category breakdown
        if (!a.apiCategoryToClasses().isEmpty()) {
            LOGGER.info("[ReForged] │ API Usage Breakdown:");
            for (var entry : a.apiCategoryToClasses().entrySet()) {
                String status = isFullyShimmed(entry.getValue()) ? "✓ SHIMMED" : "⚠ PARTIAL";
                LOGGER.info("[ReForged] │   {} — {} refs [{}]",
                        entry.getKey(), entry.getValue().size(), status);
            }
        }

        // Constructor patterns
        if (!a.constructorPatterns().isEmpty()) {
            LOGGER.info("[ReForged] │ Constructor Patterns: {}", String.join(", ", a.constructorPatterns()));
        }

        // Key annotations
        if (!a.annotations().isEmpty()) {
            Set<String> neoAnnotations = a.annotations().stream()
                    .filter(ann -> ann.contains("neoforged"))
                    .collect(Collectors.toCollection(TreeSet::new));
            if (!neoAnnotations.isEmpty()) {
                LOGGER.info("[ReForged] │ NeoForge Annotations: {}", neoAnnotations.size());
                for (String ann : neoAnnotations) {
                    LOGGER.info("[ReForged] │   @{}", ann.replace('/', '.'));
                }
            }
        }

        // Unshimmed references (top 10)
        if (!a.unshimmedRefs().isEmpty()) {
            LOGGER.warn("[ReForged] │ Unshimmed NeoForge refs ({}):", a.unshimmedRefs().size());
            int count = 0;
            for (String ref : a.unshimmedRefs()) {
                if (count++ >= 10) {
                    LOGGER.warn("[ReForged] │   ... and {} more",
                            a.unshimmedRefs().size() - 10);
                    break;
                }
                LOGGER.warn("[ReForged] │   ✗ {}", ref.replace('/', '.'));
            }
        }

        // Warnings
        if (!a.warnings().isEmpty()) {
            for (String warning : a.warnings()) {
                LOGGER.warn("[ReForged] │ ⚠ {}", warning);
            }
        }

        LOGGER.info("[ReForged] └──────────────────────────────────────────────────");
    }

    private static void logSummary(List<ModAnalysis> analyses) {
        int totalMods = analyses.size();
        long highCompat = analyses.stream().filter(a -> a.compatibilityScore() >= 0.90).count();
        long medCompat = analyses.stream().filter(a -> a.compatibilityScore() >= 0.60 && a.compatibilityScore() < 0.90).count();
        long lowCompat = analyses.stream().filter(a -> a.compatibilityScore() < 0.60).count();

        LOGGER.info("[ReForged] ═══════════════════════════════════════════════════");
        LOGGER.info("[ReForged]  Analysis Summary: {} mod(s) scanned", totalMods);
        LOGGER.info("[ReForged]    HIGH compatibility:   {}", highCompat);
        LOGGER.info("[ReForged]    MEDIUM compatibility: {}", medCompat);
        LOGGER.info("[ReForged]    LOW compatibility:    {}", lowCompat);
        LOGGER.info("[ReForged] ═══════════════════════════════════════════════════");
    }

    private static boolean isFullyShimmed(Set<String> refs) {
        return refs.stream().allMatch(ref -> {
            String pkg = ref.contains("/") ? ref.substring(0, ref.lastIndexOf('/') + 1) : "";
            return SHIMMED_PACKAGES.stream().anyMatch(pkg::startsWith);
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  ASM Scanner
    // ═══════════════════════════════════════════════════════════

    /**
     * ASM ClassVisitor that collects all NeoForge class references from
     * a mod's bytecode, including superclasses, interfaces, field types,
     * method parameter/return types, annotations, and instruction operands.
     */
    private static class DependencyScanner extends ClassVisitor {
        final Set<String> neoForgeRefs = new TreeSet<>();
        final Set<String> constructorPatterns = new LinkedHashSet<>();
        final Set<String> annotations = new TreeSet<>();
        boolean usesServiceLoader = false;
        boolean usesReflection = false;
        boolean usesMixin = false;
        boolean usesAccessTransformer = false;
        private String currentClassName;
        private boolean isModClass = false;

        DependencyScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            currentClassName = name;
            isModClass = false;
            checkAndAdd(superName);
            if (interfaces != null) {
                for (String iface : interfaces) {
                    checkAndAdd(iface);
                }
            }
            // Check for mixin
            if (name.contains("/mixin/") || name.contains("/mixins/")) {
                usesMixin = true;
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            String annClass = Type.getType(descriptor).getInternalName();
            annotations.add(annClass);
            checkAndAdd(annClass);

            if (descriptor.contains("neoforged/fml/common/Mod")) {
                isModClass = true;
            }
            if (descriptor.contains("mixin")) {
                usesMixin = true;
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                        String signature, Object value) {
            checkType(Type.getType(descriptor));
            return new FieldVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    String annClass = Type.getType(desc).getInternalName();
                    annotations.add(annClass);
                    checkAndAdd(annClass);
                    return null;
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                          String signature, String[] exceptions) {
            Type methodType = Type.getMethodType(descriptor);
            checkType(methodType.getReturnType());
            for (Type argType : methodType.getArgumentTypes()) {
                checkType(argType);
            }

            // Track @Mod constructor patterns
            if (isModClass && "<init>".equals(name)) {
                List<String> paramNames = new ArrayList<>();
                for (Type argType : methodType.getArgumentTypes()) {
                    paramNames.add(argType.getClassName());
                }
                if (paramNames.isEmpty()) {
                    constructorPatterns.add("no-arg");
                } else {
                    constructorPatterns.add("(" + String.join(", ", paramNames) + ")");
                }
            }

            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    String annClass = Type.getType(desc).getInternalName();
                    annotations.add(annClass);
                    checkAndAdd(annClass);
                    return null;
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    checkAndAdd(type);
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    checkAndAdd(owner);
                    checkType(Type.getType(desc));
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name,
                                             String desc, boolean isInterface) {
                    checkAndAdd(owner);
                    Type mt = Type.getMethodType(desc);
                    checkType(mt.getReturnType());
                    for (Type at : mt.getArgumentTypes()) {
                        checkType(at);
                    }

                    // Detect ServiceLoader usage
                    if ("java/util/ServiceLoader".equals(owner) && "load".equals(name)) {
                        usesServiceLoader = true;
                    }
                    // Detect reflection
                    if ("java/lang/Class".equals(owner) &&
                            ("forName".equals(name) || "getDeclaredMethod".equals(name) ||
                                    "getDeclaredField".equals(name))) {
                        usesReflection = true;
                    }
                }

                @Override
                public void visitLdcInsn(Object value) {
                    if (value instanceof Type t) {
                        checkType(t);
                    }
                    // Check for string references to NeoForge classes
                    if (value instanceof String s && s.startsWith("net.neoforged.")) {
                        // String-based class reference
                        neoForgeRefs.add(s.replace('.', '/'));
                    }
                }
            };
        }

        private void checkAndAdd(String internalName) {
            if (internalName != null && internalName.startsWith("net/neoforged/")) {
                neoForgeRefs.add(internalName);
            }
        }

        private void checkType(Type type) {
            if (type.getSort() == Type.OBJECT) {
                checkAndAdd(type.getInternalName());
            } else if (type.getSort() == Type.ARRAY) {
                checkType(type.getElementType());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════

    /**
     * Extract the primary modId from a JAR's neoforge.mods.toml.
     */
    private static String extractModId(JarFile jar) {
        try {
            JarEntry tomlEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
            if (tomlEntry == null) return null;

            try (InputStream is = jar.getInputStream(tomlEntry)) {
                var config = new com.electronwill.nightconfig.toml.TomlParser()
                        .parse(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
                Object modsObj = config.get("mods");
                if (modsObj instanceof List<?> modsList && !modsList.isEmpty()) {
                    Object first = modsList.get(0);
                    if (first instanceof com.electronwill.nightconfig.core.UnmodifiableConfig modConf) {
                        return modConf.get("modId");
                    }
                }
            }
        } catch (Exception e) {
            // Ignore TOML parse failures
        }
        return null;
    }

    private NeoForgeModAnalyzer() {} // no instances
}
