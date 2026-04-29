package org.xiyu.reforged.core;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Earliest ReForged hook.
 *
 * <p>Forge validates every jar in {@code mods/} before normal mods are constructed.
 * NeoForge-only jars therefore need a tiny Forge-readable descriptor before the
 * rest of ReForged can dynamically load them. This transformation service runs
 * before Forge mod discovery and patches those jars with a lowcode metadata stub.</p>
 */
public final class ReForgedTransformationService implements ITransformationService {

    private static final Logger LOGGER = createLogger();
    public static final String NAME = "reforged";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(IEnvironment environment) {
        logInfo("[ReForged] TransformationService initializing");
        patchModsFolderForForgeDiscovery();

        try {
            var registry = org.xiyu.reforged.asm.MappingRegistry.getInstance();
            logInfo("[ReForged] Loaded " + registry.getDirectCount()
                    + " direct + " + registry.getShimCount() + " shim mappings");
        } catch (Throwable t) {
            logWarn("[ReForged] Mapping registry initialization failed during early service", t);
        }
    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) throws IncompatibleEnvironmentException {
        // No launch-plugin interaction is required; bytecode rewriting happens in NeoModClassLoader.
    }

    @Override
    public List<ITransformer> transformers() {
        return List.of();
    }

    public static boolean isNeoForgeModJar(Path jarPath) {
        if (!jarPath.toString().endsWith(".jar")) return false;
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            if (jar.getEntry("META-INF/neoforge.mods.toml") != null) {
                return true;
            }
            if (jar.getEntry("META-INF/mods.toml") != null) {
                var entry = jar.getEntry("META-INF/mods.toml");
                try (var is = jar.getInputStream(entry)) {
                    String content = new String(is.readAllBytes());
                    return content.contains("neoforge") || content.contains("NeoForge");
                }
            }
        } catch (Exception e) {
            logDebug("[ReForged] Could not inspect JAR: " + jarPath + " - " + e.getMessage());
        }
        return false;
    }

    public static List<Path> discoverNeoForgeJars(Path gameDir) {
        Path neoModsDir = gameDir.resolve("neoforge-mods");
        if (!java.nio.file.Files.isDirectory(neoModsDir)) {
            logInfo("[ReForged] No neoforge-mods/ directory found at " + neoModsDir);
            return List.of();
        }
        try (Stream<Path> files = java.nio.file.Files.list(neoModsDir)) {
            List<Path> jars = files.filter(ReForgedTransformationService::isNeoForgeModJar).toList();
            logInfo("[ReForged] Discovered " + jars.size() + " NeoForge mod JAR(s) in " + neoModsDir);
            return jars;
        } catch (Exception e) {
            logWarn("[ReForged] Failed to scan neoforge-mods/", e);
            return List.of();
        }
    }

    private static void patchModsFolderForForgeDiscovery() {
        try {
            Path gameDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
            Path modsDir = gameDir.resolve("mods");
            int patched = NeoForgeModPatcher.patchAll(modsDir);
            if (patched > 0) {
                logInfo("[ReForged] Patched " + patched + " NeoForge jar(s) in " + modsDir);
            }
        } catch (Throwable t) {
            logWarn("[ReForged] Early NeoForge jar patching failed", t);
        }
    }

    private static Logger createLogger() {
        try {
            return LogUtils.getLogger();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void logInfo(String message) {
        if (LOGGER != null) LOGGER.info(message);
        else System.out.println(message);
    }

    private static void logDebug(String message) {
        if (LOGGER != null) LOGGER.debug(message);
    }

    private static void logWarn(String message, Throwable t) {
        if (LOGGER != null) LOGGER.warn(message, t);
        else {
            System.err.println(message);
            if (t != null) t.printStackTrace();
        }
    }
}
