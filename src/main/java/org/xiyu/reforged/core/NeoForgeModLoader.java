package org.xiyu.reforged.core;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * NeoForgeModLoader — Runtime discovery and loading of NeoForge mods.
 *
 * <h3>How it works:</h3>
 * <ol>
 *   <li>Scans the {@code mods/} folder for JARs containing {@code META-INF/neoforge.mods.toml}
 *       but NOT {@code META-INF/mods.toml} (to avoid interfering with real Forge mods)</li>
 *   <li>Creates a {@link URLClassLoader} parented by the game's classloader,
 *       containing the NeoForge mod JARs</li>
 *   <li>Uses ASM to scan for {@code @net.neoforged.fml.common.Mod} annotated classes</li>
 *   <li>Instantiates each mod class, passing the event bus (wrapped as NeoForge's IEventBus)</li>
 * </ol>
 *
 * <p>This runs during ReForged's {@code @Mod} constructor, which fires before registry events.</p>
 */
public final class NeoForgeModLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NEO_MOD_ANNOTATION = "Lnet/neoforged/fml/common/Mod;";
    private static final List<Object> loadedModInstances = new ArrayList<>();

    /** NeoForge JAR paths that were successfully loaded (for resource pack registration). */
    private static final List<Path> loadedNeoJarPaths = new ArrayList<>();

    /** Stored reference to Forge's mod event bus — used to dispatch NeoForge mod bus events. */
    private static IEventBus storedModEventBus;

    /** Default metadata used when TOML parsing fails or a modId is not found in TOML. */
    private static final ModMetadata DEFAULT_METADATA =
            new ModMetadata("Unknown Mod", "1.0.0", "", "Unknown", null);

    private record ModMetadata(String displayName, String version,
                                String description, String license, String logoFile) {}

    /**
     * Get the paths of all successfully loaded NeoForge mod JARs.
     * Used by the resource pack injector to register JAR resources.
     */
    public static List<Path> getLoadedNeoJarPaths() {
        return Collections.unmodifiableList(loadedNeoJarPaths);
    }

    /**
     * Dispatch a NeoForge mod-bus event to all NeoForge mod listeners.
     *
     * <p>Since NeoForge's {@code net.neoforged.bus.api.Event} extends Forge's
     * {@code net.minecraftforge.eventbus.api.Event}, NeoForge events can be posted
     * directly on the Forge mod event bus. NeoForge mod handlers registered via
     * {@link NeoForgeEventBusAdapter} will receive them.</p>
     *
     * @param event a NeoForge event instance (must extend Forge's Event through inheritance)
     */
    public static void dispatchNeoForgeModEvent(net.minecraftforge.eventbus.api.Event event) {
        if (storedModEventBus == null) {
            LOGGER.warn("[ReForged] Cannot dispatch NeoForge mod event {} — mod bus not yet initialised",
                    event.getClass().getSimpleName());
            return;
        }
        try {
            storedModEventBus.post(event);
        } catch (Throwable t) {
            LOGGER.error("[ReForged] Failed to dispatch NeoForge mod event {}: {}",
                    event.getClass().getSimpleName(), t.getMessage(), t);
        }
    }

    /**
     * Discover and load all NeoForge mods from the given directory.
     *
     * @param modsDir   the mods directory to scan
     * @param modBus    Forge's mod event bus (from FMLJavaModLoadingContext)
     */
    public static void loadAll(Path modsDir, IEventBus modBus) {
        storedModEventBus = modBus;
        if (!Files.isDirectory(modsDir)) {
            LOGGER.debug("[ReForged] Mods directory not found: {}", modsDir);
            return;
        }

        // Phase 1: Find NeoForge mod JARs
        List<Path> neoJars = discoverNeoForgeJars(modsDir);
        if (neoJars.isEmpty()) {
            LOGGER.info("[ReForged] No NeoForge mods found in {}", modsDir);
            return;
        }
        LOGGER.info("[ReForged] Found {} NeoForge mod JAR(s): {}", neoJars.size(),
                neoJars.stream().map(p -> p.getFileName().toString()).toList());

        // Phase 2: Create classloader with all NeoForge JARs
        URLClassLoader neoClassLoader = createClassLoader(neoJars);
        if (neoClassLoader == null) return;

        // Phase 2.5: Open game module packages to the URLClassLoader's unnamed module
        // so NeoForge mod classes can access Minecraft/Forge classes across the module boundary.
        openGameModulesToClassLoader(neoClassLoader);

        // Phase 2.75: Process enum extensions from NeoForge mods
        // Must happen BEFORE any mod classes are loaded / initialized
        EnumExtensionHandler.processAll(neoJars, neoClassLoader);

        // Phase 3: Wrap the event bus as NeoForge's IEventBus
        net.neoforged.bus.api.IEventBus busAdapter = NeoForgeEventBusAdapter.wrap(modBus);

        // Make the bus available to NeoForge ModContainer wrappers
        net.neoforged.fml.ModContainer.setGlobalModBus(busAdapter);

        // Phase 3.5: Temporarily unfreeze the root registry so NeoForge mods that
        // directly call Registry.register() on BuiltInRegistries.REGISTRY (e.g. Create's
        // CreateBuiltInRegistries) don't hit "Registry is already frozen".
        Field rootFrozenField = null;
        boolean rootWasFrozen = false;
        try {
            Object rootRegistry = net.minecraft.core.registries.BuiltInRegistries.REGISTRY;
            rootFrozenField = findFrozenField(rootRegistry.getClass());
            if (rootFrozenField != null) {
                rootFrozenField.setAccessible(true);
                rootWasFrozen = rootFrozenField.getBoolean(rootRegistry);
                if (rootWasFrozen) {
                    rootFrozenField.setBoolean(rootRegistry, false);
                    LOGGER.info("[ReForged] Temporarily unfroze root registry for NeoForge mod loading");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Could not unfreeze root registry: {}", e.getMessage());
        }

        // Phase 4: Scan and load each mod, collecting containers for ModList injection
        List<ModListInjector.NeoModContainer> neoContainers = new ArrayList<>();
        List<String> failedMods = new ArrayList<>();
        for (Path jar : neoJars) {
            try {
                int before = loadedModInstances.size();
                loadModsFromJar(jar, neoClassLoader, busAdapter, modBus, neoContainers);
                if (loadedModInstances.size() > before) {
                    // At least one mod from this JAR loaded successfully — track it for resource packs
                    loadedNeoJarPaths.add(jar);
                }
            } catch (Throwable e) {
                String jarName = jar.getFileName().toString();
                failedMods.add(jarName);
                LOGGER.error("[ReForged] Failed to load NeoForge mod from {}", jarName, e);
            }
        }

        // Phase 4.5: Re-freeze the root registry now that NeoForge mod construction is complete
        if (rootWasFrozen && rootFrozenField != null) {
            try {
                rootFrozenField.setBoolean(net.minecraft.core.registries.BuiltInRegistries.REGISTRY, true);
                LOGGER.info("[ReForged] Re-froze root registry after NeoForge mod loading");
            } catch (Exception e) {
                LOGGER.warn("[ReForged] Could not re-freeze root registry: {}", e.getMessage());
            }
        }

        if (!failedMods.isEmpty()) {
            LOGGER.warn("[ReForged] {} NeoForge mod JAR(s) had loading failures: {} — continuing without them",
                    failedMods.size(), failedMods);
        }
        LOGGER.info("[ReForged] Loaded {} NeoForge mod instance(s)", loadedModInstances.size());

        // Phase 5: Register NeoForge mods in Forge's ModList so they appear in the Mods screen
        ModListInjector.inject(neoContainers);

        // Phase 6: Auto-register @EventBusSubscriber classes from NeoForge JARs
        registerEventBusSubscribers(neoJars, neoClassLoader, modBus);
    }

    /**
     * Find the "frozen" field in a MappedRegistry (tries official + SRG names).
     */
    private static Field findFrozenField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : new String[]{"frozen", "f_205845_"}) {
                try {
                    Field f = current.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {}
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * Find JARs in modsDir that have neoforge.mods.toml but not mods.toml.
     */
    private static List<Path> discoverNeoForgeJars(Path modsDir) {
        List<Path> result = new ArrayList<>();
        try (var stream = Files.list(modsDir)) {
            for (Path path : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                try (JarFile jar = new JarFile(path.toFile())) {
                    boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                    boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                    if (hasNeo && hasForge) {
                        LOGGER.info("[ReForged] Skipping Forgix-packaged JAR {} — has both mods.toml and neoforge.mods.toml, letting Forge handle it",
                                path.getFileName());
                    } else if (hasNeo && !hasForge) {
                        result.add(path);
                    }
                } catch (Exception e) {
                    LOGGER.debug("[ReForged] Skipping {}: {}", path.getFileName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Error scanning mods directory", e);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  @EventBusSubscriber auto-registration
    // ═══════════════════════════════════════════════════════════

    private static final String EVENT_BUS_SUBSCRIBER_DESC = "Lnet/neoforged/fml/common/EventBusSubscriber;";

    /**
     * Scan all NeoForge mod JARs for classes annotated with {@code @EventBusSubscriber}
     * and register their {@code @SubscribeEvent} methods on the appropriate event bus.
     *
     * <ul>
     *   <li>{@code bus = MOD} → registers on Forge's mod event bus</li>
     *   <li>{@code bus = GAME} (default) → registers on {@code MinecraftForge.EVENT_BUS}</li>
     * </ul>
     *
     * <p>Respects the {@code value} (Dist) filter: CLIENT-only classes are skipped on servers.</p>
     */
    private static void registerEventBusSubscribers(List<Path> jars, URLClassLoader classLoader,
                                                     IEventBus modBus) {
        boolean isClient = net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient();

        for (Path jarPath : jars) {
            try {
                List<SubscriberInfo> subscribers = scanForEventBusSubscribers(jarPath);
                for (SubscriberInfo info : subscribers) {
                    // Check Dist filter
                    if (!info.dists.isEmpty()) {
                        boolean matches = info.dists.stream().anyMatch(d ->
                                ("CLIENT".equals(d) && isClient) ||
                                ("DEDICATED_SERVER".equals(d) && !isClient));
                        if (!matches) {
                            LOGGER.debug("[ReForged] Skipping {} — Dist filter mismatch", info.className);
                            continue;
                        }
                    }

                    try {
                        Class<?> subscriberClass = classLoader.loadClass(info.className);

                        // Determine target bus
                        net.minecraftforge.eventbus.api.IEventBus targetBus;
                        if ("MOD".equals(info.bus)) {
                            targetBus = modBus;
                        } else {
                            // GAME / FORGE → MinecraftForge.EVENT_BUS
                            targetBus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
                        }

                        // Register the class (static methods with @SubscribeEvent)
                        NeoForgeEventBusAdapter.handleRegister(targetBus, subscriberClass);

                        LOGGER.info("[ReForged] ✓ Auto-registered @EventBusSubscriber: {} (bus={}, dist={})",
                                subscriberClass.getSimpleName(), info.bus, info.dists);
                    } catch (Throwable t) {
                        // Catch Throwable (not just Exception) — NoClassDefFoundError can
                        // propagate from Class.getMethods/getDeclaredMethods when the class
                        // references NeoForge event types without shims.
                        LOGGER.warn("[ReForged] Skipping subscriber class {} — {}",
                                info.className, t.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to scan {} for @EventBusSubscriber", jarPath.getFileName(), e);
            }
        }
    }

    /**
     * ASM-scan a JAR for classes annotated with {@code @net.neoforged.fml.common.EventBusSubscriber}.
     */
    private static List<SubscriberInfo> scanForEventBusSubscribers(Path jarPath) {
        List<SubscriberInfo> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    EventBusSubscriberScanner scanner = new EventBusSubscriberScanner();
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    if (scanner.isSubscriber) {
                        String className = entry.getName().replace('/', '.').replace(".class", "");
                        result.add(new SubscriberInfo(className, scanner.bus, scanner.dists));
                    }
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR for subscribers: {}", jarPath.getFileName(), e);
        }
        return result;
    }

    private record SubscriberInfo(String className, String bus, List<String> dists) {}

    /**
     * ASM visitor that detects {@code @EventBusSubscriber} and extracts bus + value (Dist) fields.
     */
    private static class EventBusSubscriberScanner extends ClassVisitor {
        boolean isSubscriber = false;
        String bus = "GAME";     // default bus
        List<String> dists = new ArrayList<>();

        EventBusSubscriberScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (EVENT_BUS_SUBSCRIBER_DESC.equals(descriptor)) {
                isSubscriber = true;
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitEnum(String name, String enumDesc, String value) {
                        if ("bus".equals(name)) {
                            bus = value;  // "MOD" or "GAME"
                        }
                    }

                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        if ("value".equals(name)) {
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitEnum(String n, String desc, String value) {
                                    dists.add(value);  // "CLIENT" or "DEDICATED_SERVER"
                                }
                            };
                        }
                        return super.visitArray(name);
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  JPMS module access: open game-layer packages to unnamed module
    // ═══════════════════════════════════════════════════════════

    /**
     * Open all packages from game-layer modules (minecraft, forge, etc.) to the
     * unnamed module of the given classloader.
     *
     * <p><b>Why this is needed:</b> NeoForge mod classes are loaded by a
     * {@link URLClassLoader} and end up in its <em>unnamed</em> module. Minecraft
     * and Forge classes live in <em>named</em> JPMS modules ({@code minecraft@1.21.1},
     * {@code forge}, etc.). The JVM blocks cross-module access unless the named
     * module explicitly exports its packages to the reader module.</p>
     *
     * <p>We use the same technique as Forge's own {@code ForgeMod.addOpen()}:
     * reflective access to {@code Module.implAddExportsOrOpens} via
     * {@code sun.misc.Unsafe} to bypass module encapsulation.</p>
     *
     * @param neoClassLoader the URLClassLoader whose unnamed module needs access
     */
    @SuppressWarnings("deprecation") // Unsafe.objectFieldOffset is deprecated since JDK 18 but still functional
    private static void openGameModulesToClassLoader(URLClassLoader neoClassLoader) {
        try {
            // ── Step 1: Obtain sun.misc.Unsafe ───────────────────────────────
            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);

            // ── Step 2: Find Class.module field offset via Unsafe ────────────
            long moduleFieldOffset = -1;
            for (Field f : Class.class.getDeclaredFields()) {
                if ("module".equals(f.getName())) {
                    moduleFieldOffset = unsafe.objectFieldOffset(f);
                    break;
                }
            }
            if (moduleFieldOffset == -1) {
                LOGGER.error("[ReForged] Cannot find 'module' field in java.lang.Class — " +
                        "module access hack unavailable, NeoForge mods may crash");
                return;
            }

            // ── Step 3: Get Module.implAddExportsOrOpens reflectively ────────
            // We must bypass setAccessible() module checks by temporarily
            // pretending our class belongs to java.base (same trick as
            // Forge's UnsafeHacks and cpw.mods.securejarhandler).
            Object originalModule = unsafe.getObject(NeoForgeModLoader.class, moduleFieldOffset);
            Object javaBaseModule = unsafe.getObject(Object.class, moduleFieldOffset); // Object.class → java.base
            unsafe.putObject(NeoForgeModLoader.class, moduleFieldOffset, javaBaseModule);

            Method implAddExportsOrOpens;
            try {
                implAddExportsOrOpens = Module.class.getDeclaredMethod(
                        "implAddExportsOrOpens",
                        String.class,   // package name
                        Module.class,   // target module
                        boolean.class,  // open (true) vs export-only (false)
                        boolean.class   // syncVM
                );
                implAddExportsOrOpens.setAccessible(true);
            } finally {
                // Restore original module immediately
                unsafe.putObject(NeoForgeModLoader.class, moduleFieldOffset, originalModule);
            }

            // ── Step 4: Collect all named modules from game / parent layers ──
            Module unnamedModule = neoClassLoader.getUnnamedModule();
            ClassLoader gameClassLoader = NeoForgeModLoader.class.getClassLoader();

            Set<Module> modulesToOpen = new LinkedHashSet<>();

            // Try to discover the game ModuleLayer by loading a known MC class
            String[] probeClasses = {
                    "net.minecraft.world.entity.Entity",       // minecraft module
                    "net.minecraftforge.common.MinecraftForge", // forge module
                    "net.minecraftforge.fml.ModList",           // fml module
            };
            for (String probeName : probeClasses) {
                try {
                    Class<?> probe = Class.forName(probeName, false, gameClassLoader);
                    Module mod = probe.getModule();
                    if (mod.isNamed()) {
                        modulesToOpen.add(mod);
                        // Also grab all sibling modules from the same layer
                        ModuleLayer layer = mod.getLayer();
                        if (layer != null) {
                            for (Module sibling : layer.modules()) {
                                if (sibling.isNamed()) {
                                    modulesToOpen.add(sibling);
                                }
                            }
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                    // Probe class not available — skip
                }
            }

            if (modulesToOpen.isEmpty()) {
                LOGGER.warn("[ReForged] No named game modules found — " +
                        "module access hack skipped (classes may already be in unnamed module)");
                return;
            }

            // ── Step 5: Open every package of every game module ──────────────
            int totalPackages = 0;
            for (Module module : modulesToOpen) {
                Set<String> packages = module.getPackages();
                for (String pkg : packages) {
                    try {
                        implAddExportsOrOpens.invoke(
                                module,
                                pkg,
                                unnamedModule,
                                /* open = */ true,
                                /* syncVM = */ true
                        );
                    } catch (Exception e) {
                        LOGGER.debug("[ReForged] Failed to open {}/{} — {}",
                                module.getName(), pkg, e.getMessage());
                    }
                }
                totalPackages += packages.size();
            }

            LOGGER.info("[ReForged] Opened {} packages across {} modules to NeoForge classloader's unnamed module",
                    totalPackages, modulesToOpen.size());

        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to open game module packages — " +
                    "NeoForge mods extending Minecraft classes will likely crash with IllegalAccessError", e);
        }
    }

    /**
     * Create a URLClassLoader for the NeoForge mod JARs.
     * Parent is the current classloader (game + Forge + ReForged).
     * <p>
     * Also extracts nested Jar-in-Jar (JiJ) dependencies from
     * {@code META-INF/jarjar/} inside each mod JAR and adds them
     * to the classloader so library classes are available.
     */
    private static URLClassLoader createClassLoader(List<Path> jars) {
        try {
            List<URL> urls = new ArrayList<>();
            // Add top-level mod JARs
            for (Path jar : jars) {
                urls.add(jar.toUri().toURL());
            }
            // Extract Jar-in-Jar (JiJ) dependencies
            Path jijTemp = Files.createTempDirectory("reforged-jij-");
            jijTemp.toFile().deleteOnExit();
            for (Path jar : jars) {
                try (JarFile jarFile = new JarFile(jar.toFile())) {
                    var entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar") && !entry.isDirectory()) {
                            // Extract nested JAR to temp directory
                            String fileName = name.substring(name.lastIndexOf('/') + 1);
                            Path extracted = jijTemp.resolve(fileName);
                            try (InputStream is = jarFile.getInputStream(entry)) {
                                Files.copy(is, extracted, StandardCopyOption.REPLACE_EXISTING);
                            }
                            extracted.toFile().deleteOnExit();
                            urls.add(extracted.toUri().toURL());
                            LOGGER.info("[ReForged] Extracted JiJ dependency: {} from {}", fileName, jar.getFileName());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Failed to extract JiJ from {}: {}", jar.getFileName(), e.getMessage());
                }
            }
            return new URLClassLoader(urls.toArray(new URL[0]), NeoForgeModLoader.class.getClassLoader()) {
                /**
                 * Override findClass so that any thread loading a NeoForge mod class
                 * automatically has its context classloader set to this URLClassLoader.
                 * This is critical for ServiceLoader (used by Create/Catnip) to find
                 * service implementations in NeoForge mod JARs, even when invoked
                 * from ForkJoinPool worker threads that have a different default
                 * context classloader.
                 */
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    Thread.currentThread().setContextClassLoader(this);
                    return super.findClass(name);
                }
            };
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to create classloader", e);
            return null;
        }
    }

    /**
     * Scan a JAR for @Mod annotated classes and instantiate them.
     * Pre-creates containers for each mod and sets them as the active Forge container
     * during construction, so configs register under the correct mod ID.
     * Successfully loaded containers are added to {@code collector} for later ModList injection.
     */
    private static void loadModsFromJar(Path jarPath, URLClassLoader classLoader,
                                         net.neoforged.bus.api.IEventBus busAdapter, IEventBus modBus,
                                         List<ModListInjector.NeoModContainer> collector) throws Exception {
        // Scan for @Mod classes using ASM (without loading classes)
        List<ModInfo> modClasses = scanForModClasses(jarPath);
        // Parse TOML metadata from the JAR
        Map<String, ModMetadata> metadata = parseModMetadata(jarPath);

        // Perform a full annotation scan of the JAR so NeoForge mods
        // (e.g. Twilight Forest BeanContext) can find their annotated classes
        // through ModFileScanData.getAnnotatedBy().
        net.minecraftforge.forgespi.language.ModFileScanData jarScanData = scanJarAnnotations(jarPath);
        LOGGER.info("[ReForged] Scanned {} annotations and {} classes from {}",
                jarScanData.getAnnotations().size(), jarScanData.getClasses().size(),
                jarPath.getFileName());

        for (ModInfo info : modClasses) {
            LOGGER.info("[ReForged] Loading NeoForge mod: '{}' (class: {})", info.modId, info.className);
            try {
                Class<?> modClass = classLoader.loadClass(info.className);

                // Create NeoModData and Forge container BEFORE construction
                ModMetadata meta = metadata.getOrDefault(info.modId, DEFAULT_METADATA);
                ModListInjector.NeoModData neoModData = new ModListInjector.NeoModData(
                        info.modId,
                        meta.displayName(),
                        meta.version(),
                        meta.description(),
                        meta.license(),
                        meta.logoFile(),
                        null,  // instance not yet available
                        jarScanData,
                        jarPath
                );
                ModListInjector.NeoModContainer neoContainer = ModListInjector.createContainer(neoModData);

                // Pre-register the NeoForge mod file info so ModList.getModFileById()
                // works during static class initialization (before inject() completes)
                var forgeFileInfo = neoContainer.getModInfo().getOwningFile();
                if (forgeFileInfo != null) {
                    var neoFileInfo = net.neoforged.neoforgespi.language.IModFileInfo.wrap(forgeFileInfo);
                    net.neoforged.fml.ModList.registerNeoModFileInfo(info.modId, neoFileInfo);
                }

                // Swap Forge's active container to our NeoForge container so configs
                // registered during construction use the correct mod ID
                var forgeCtx = net.minecraftforge.fml.ModLoadingContext.get();
                var oldContainer = forgeCtx.getActiveContainer();
                forgeCtx.setActiveContainer(neoContainer);

                // Inject the container into ModList BEFORE construction,
                // so mods that look themselves up (e.g. TwilightForest BeanContext)
                // can find their container during static initialization.
                ModListInjector.inject(List.of(neoContainer));

                Object instance;
                try {
                    instance = instantiateMod(modClass, busAdapter, modBus);
                } finally {
                    // Restore the original active container
                    forgeCtx.setActiveContainer(oldContainer);
                }

                if (instance != null) {
                    loadedModInstances.add(instance);
                    neoContainer.setModInstance(instance);
                    collector.add(neoContainer);
                    LOGGER.info("[ReForged] ✓ Successfully loaded NeoForge mod '{}'", info.modId);
                }
            } catch (Throwable e) {
                // Catch Throwable (not just Exception) to handle ExceptionInInitializerError,
                // NoClassDefFoundError, VerifyError, etc. from NeoForge mod static initializers.
                // We log the failure but continue loading remaining mod classes from this JAR.
                String cause = e.getMessage();
                if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                    cause = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
                }
                LOGGER.error("[ReForged] ✗ Failed to load mod class '{}': {} — skipping this mod, continuing",
                        info.className, cause, e);
            }
        }
    }

    /**
     * Use ASM to scan a JAR for classes annotated with @net.neoforged.fml.common.Mod.
     */
    private static List<ModInfo> scanForModClasses(Path jarPath) {
        List<ModInfo> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    ModAnnotationScanner scanner = new ModAnnotationScanner();
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    if (scanner.modId != null) {
                        String className = entry.getName()
                                .replace('/', '.')
                                .replace(".class", "");
                        result.add(new ModInfo(scanner.modId, className));
                    }
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR: {}", jarPath.getFileName(), e);
        }
        return result;
    }

    /**
     * Perform a full ASM scan of a JAR, collecting ALL annotation data and class hierarchy
     * information into a Forge {@link net.minecraftforge.forgespi.language.ModFileScanData}.
     *
     * <p>This allows NeoForge mods (like Twilight Forest's BeanContext) to discover their
     * annotated classes (e.g. {@code @BeanProcessor}, {@code @Bean}, {@code @Component})
     * through the standard {@code ModFileScanData.getAnnotatedBy()} / {@code getAnnotations()} APIs.</p>
     */
    private static net.minecraftforge.forgespi.language.ModFileScanData scanJarAnnotations(Path jarPath) {
        net.minecraftforge.forgespi.language.ModFileScanData scanData =
                new net.minecraftforge.forgespi.language.ModFileScanData();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    FullAnnotationScanner scanner = new FullAnnotationScanner(scanData);
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR annotations: {}", jarPath.getFileName(), e);
        }
        return scanData;
    }

    /**
     * ASM visitor that collects ALL annotations (class-level, method-level, field-level)
     * and class hierarchy data into a Forge ModFileScanData.
     *
     * <p>This mirrors what NeoForge's ModFile scanner does, allowing third-party mods
     * that rely on annotation scanning (like Twilight Forest's beanification framework)
     * to work correctly under ReForged.</p>
     */
    private static class FullAnnotationScanner extends ClassVisitor {
        private final net.minecraftforge.forgespi.language.ModFileScanData scanData;
        private Type classType;

        FullAnnotationScanner(net.minecraftforge.forgespi.language.ModFileScanData scanData) {
            super(Opcodes.ASM9);
            this.scanData = scanData;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.classType = Type.getObjectType(name);
            Type parentType = superName != null ? Type.getObjectType(superName) : Type.getType(Object.class);
            Set<Type> ifaceTypes = new LinkedHashSet<>();
            if (interfaces != null) {
                for (String iface : interfaces) {
                    ifaceTypes.add(Type.getObjectType(iface));
                }
            }
            scanData.getClasses().add(
                    new net.minecraftforge.forgespi.language.ModFileScanData.ClassData(
                            classType, parentType, ifaceTypes));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return new ScanAnnotationVisitor(descriptor, java.lang.annotation.ElementType.TYPE,
                    classType, "", scanData);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                        String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.FIELD,
                            classType, name, scanData);
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                          String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.METHOD,
                            classType, name, scanData);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.PARAMETER,
                            classType, name, scanData);
                }
            };
        }
    }

    /**
     * ASM AnnotationVisitor that collects annotation data (including nested values,
     * arrays, and enum constants) into a Map, then adds it to the scan data.
     */
    private static class ScanAnnotationVisitor extends AnnotationVisitor {
        private final Type annotationType;
        private final java.lang.annotation.ElementType targetType;
        private final Type classType;
        private final String memberName;
        private final net.minecraftforge.forgespi.language.ModFileScanData scanData;
        private final Map<String, Object> values = new LinkedHashMap<>();

        ScanAnnotationVisitor(String descriptor, java.lang.annotation.ElementType targetType,
                              Type classType, String memberName,
                              net.minecraftforge.forgespi.language.ModFileScanData scanData) {
            super(Opcodes.ASM9);
            this.annotationType = Type.getType(descriptor);
            this.targetType = targetType;
            this.classType = classType;
            this.memberName = memberName;
            this.scanData = scanData;
        }

        @Override
        public void visit(String name, Object value) {
            values.put(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            values.put(name, new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(descriptor, value));
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            List<Object> list = new ArrayList<>();
            values.put(name, list);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String n, Object value) {
                    list.add(value);
                }

                @Override
                public void visitEnum(String n, String desc, String value) {
                    list.add(new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(desc, value));
                }
            };
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            // Nested annotations — store as a map
            Map<String, Object> nested = new LinkedHashMap<>();
            values.put(name, nested);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String n, Object value) {
                    nested.put(n, value);
                }

                @Override
                public void visitEnum(String n, String desc, String value) {
                    nested.put(n, new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(desc, value));
                }
            };
        }

        @Override
        public void visitEnd() {
            scanData.getAnnotations().add(
                    new net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData(
                            annotationType, targetType, classType, memberName, values));
        }
    }

    /**
     * Instantiate a NeoForge mod class, trying multiple constructor patterns.
     */
    private static Object instantiateMod(Class<?> modClass, net.neoforged.bus.api.IEventBus busAdapter,
                                          IEventBus modBus) throws Exception {
        // Pattern 1: (net.neoforged.bus.api.IEventBus)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(net.neoforged.bus.api.IEventBus.class);
            ctor.setAccessible(true);
            return ctor.newInstance(busAdapter);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 2: (net.minecraftforge.eventbus.api.IEventBus) — some mods use Forge's type directly
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(IEventBus.class);
            ctor.setAccessible(true);
            return ctor.newInstance(modBus);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 3: (net.neoforged.bus.api.IEventBus, net.neoforged.fml.ModContainer)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.bus.api.IEventBus.class,
                    net.neoforged.fml.ModContainer.class);
            ctor.setAccessible(true);
            // Create a dummy ModContainer
            net.neoforged.fml.ModContainer container =
                    new net.neoforged.fml.ModContainer(
                            net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
            return ctor.newInstance(busAdapter, container);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 5: (net.neoforged.bus.api.IEventBus, net.neoforged.api.distmarker.Dist)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.bus.api.IEventBus.class,
                    net.neoforged.api.distmarker.Dist.class);
            ctor.setAccessible(true);
            net.neoforged.api.distmarker.Dist currentDist =
                    net.neoforged.fml.loading.FMLEnvironment.dist;
            return ctor.newInstance(busAdapter, currentDist);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 4: no-arg constructor
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {}

        LOGGER.error("[ReForged] No compatible constructor found for {}", modClass.getName());
        return null;
    }

    /**
     * ASM visitor that scans for @Mod annotation and extracts the modId.
     */
    private static class ModAnnotationScanner extends ClassVisitor {
        String modId = null;

        ModAnnotationScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (NEO_MOD_ANNOTATION.equals(descriptor)) {
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name) && value instanceof String s) {
                            modId = s;
                        }
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    private record ModInfo(String modId, String className) {}

    /**
     * Parse {@code META-INF/neoforge.mods.toml} from a JAR to extract mod display metadata.
     * Falls back gracefully if TOML is missing or unparseable.
     *
     * @return map of modId → metadata; empty map on failure
     */
    private static Map<String, ModMetadata> parseModMetadata(Path jarPath) {
        Map<String, ModMetadata> result = new HashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // Resolve JAR version from manifest (for ${file.jarVersion} substitution)
            String jarVersion = "1.0.0";
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String implVer = manifest.getMainAttributes().getValue("Implementation-Version");
                if (implVer != null && !implVer.isEmpty()) jarVersion = implVer;
            }

            JarEntry tomlEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
            if (tomlEntry == null) return result;

            try (InputStream is = jar.getInputStream(tomlEntry)) {
                var config = new TomlParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
                String license = config.getOrElse("license", "Unknown");

                Object modsObj = config.get("mods");
                if (modsObj instanceof List<?> modsList) {
                    for (Object entry : modsList) {
                        if (!(entry instanceof UnmodifiableConfig modConf)) continue;
                        String modId = modConf.get("modId");
                        if (modId == null) continue;

                        String displayName = modConf.getOrElse("displayName", modId);
                        String version = modConf.getOrElse("version", jarVersion);
                        String description = modConf.getOrElse("description", "");
                        String logoFile = modConf.get("logoFile");

                        // Resolve template variables like ${file.jarVersion}
                        if (version.contains("${")) version = jarVersion;

                        result.put(modId, new ModMetadata(displayName, version, description, license, logoFile));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] Could not parse TOML metadata from {}: {}",
                    jarPath.getFileName(), e.getMessage());
        }
        return result;
    }
}
