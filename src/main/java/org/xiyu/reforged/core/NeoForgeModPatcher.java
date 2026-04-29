package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Patches NeoForge JARs before Forge's own mod-folder scanner sees them.
 *
 * <p>The injected {@code META-INF/mods.toml} is a lowcode discovery descriptor:
 * Forge can record the mod id, but it will not execute the NeoForge entrypoint.
 * ReForged still loads the real mod from {@code neoforge.mods.toml} later.</p>
 */
public final class NeoForgeModPatcher {

    private static final Logger LOGGER;
    private static final byte[] DEFAULT_PACK_MCMETA = """
            {
              "pack": {
                "description": "ReForged NeoForge mod resources",
                "pack_format": 32
              }
            }
            """.getBytes(StandardCharsets.UTF_8);

    static {
        Logger tempLogger;
        try {
            tempLogger = LogUtils.getLogger();
        } catch (Throwable t) {
            tempLogger = null;
        }
        LOGGER = tempLogger;
    }

    private NeoForgeModPatcher() {}

    private static void log(String msg) {
        if (LOGGER != null) LOGGER.info(msg);
        else System.out.println(msg);
    }

    private static void warn(String msg, Throwable t) {
        if (LOGGER != null) LOGGER.warn(msg, t);
        else {
            System.err.println(msg);
            if (t != null) t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: NeoForgeModPatcher <modsDir>");
            System.exit(1);
        }
        Path modsDir = Path.of(args[0]);
        int patched = patchAll(modsDir);
        log("[ReForged] NeoForgeModPatcher: " + patched + " NeoForge mod(s) patched in " + modsDir);
    }

    public static int patchAll(Path modsDir) {
        if (!Files.isDirectory(modsDir)) return 0;

        int count = 0;
        try (var stream = Files.list(modsDir)) {
            for (Path jar : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                if (patchIfNeeded(jar)) count++;
            }
        } catch (Exception e) {
            warn("[ReForged] Error scanning mods directory: " + modsDir, e);
        }
        return count;
    }

    public static boolean patchIfNeeded(Path jarPath) {
        Path backup = jarPath.resolveSibling(jarPath.getFileName() + ".neoforge-original");
        Path sourcePath = Files.exists(backup) ? backup : jarPath;
        String neoContent;
        byte[][] entryData;
        String[] entryNames;
        boolean hasPackMeta = false;

        try (JarFile jar = new JarFile(sourcePath.toFile())) {
            JarEntry neoEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
            if (neoEntry == null) return false;

            JarEntry existingForgeDescriptor = jar.getJarEntry("META-INF/mods.toml");
            if (existingForgeDescriptor != null && !isReForgedDiscoveryDescriptor(jar, existingForgeDescriptor)) {
                return false;
            }

            try (InputStream is = jar.getInputStream(neoEntry)) {
                neoContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            var entries = jar.entries();
            var nameList = new ArrayList<String>();
            var dataList = new ArrayList<byte[]>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (shouldDropFromForgePlaceholder(entryName)) {
                    continue;
                }

                if ("pack.mcmeta".equals(entryName)) {
                    hasPackMeta = true;
                }

                nameList.add(entryName);
                if (entry.isDirectory()) {
                    dataList.add(new byte[0]);
                    continue;
                }

                try (InputStream is = jar.getInputStream(entry)) {
                    byte[] bytes = is.readAllBytes();
                    if ("META-INF/MANIFEST.MF".equalsIgnoreCase(entryName)) {
                        bytes = sanitizeManifest(bytes);
                    }
                    dataList.add(bytes);
                }
            }
            entryNames = nameList.toArray(new String[0]);
            entryData = dataList.toArray(new byte[0][]);
        } catch (Exception e) {
            warn("[ReForged] Failed to read JAR: " + jarPath.getFileName(), e);
            return false;
        }

        String forgeContent = ModDescriptorConverter.convertForForgeDiscovery(neoContent);
        log("[ReForged] Patching NeoForge mod: " + jarPath.getFileName());

        Path tempJar = jarPath.resolveSibling(jarPath.getFileName() + ".tmp");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(tempJar.toFile()))) {
            for (int i = 0; i < entryNames.length; i++) {
                out.putNextEntry(new JarEntry(entryNames[i]));
                if (entryData[i].length > 0) {
                    out.write(entryData[i]);
                }
                out.closeEntry();
            }

            out.putNextEntry(new JarEntry("META-INF/mods.toml"));
            out.write(forgeContent.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();

            if (!hasPackMeta) {
                out.putNextEntry(new JarEntry("pack.mcmeta"));
                out.write(DEFAULT_PACK_MCMETA);
                out.closeEntry();
            }
        } catch (Exception e) {
            warn("[ReForged] Failed to write patched JAR: " + jarPath.getFileName(), e);
            return false;
        }

        try {
            if (!Files.exists(backup)) {
                Files.copy(jarPath, backup);
            }
            Files.move(tempJar, jarPath, StandardCopyOption.REPLACE_EXISTING);
            log("[ReForged] Patched: " + jarPath.getFileName() + " (original backed up as .neoforge-original)");
            return true;
        } catch (Exception e) {
            warn("[ReForged] Failed to replace JAR: " + jarPath.getFileName(), e);
            return false;
        }
    }

    private static boolean shouldDropFromForgePlaceholder(String entryName) {
        String lower = entryName.toLowerCase(java.util.Locale.ROOT);
        return "meta-inf/mods.toml".equals(lower)
                || lower.endsWith(".class")
                || lower.startsWith("meta-inf/services/")
                || lower.startsWith("meta-inf/jarjar/")
                || lower.startsWith("meta-inf/accesstransformer")
                || lower.endsWith(".mixins.json")
                || lower.endsWith(".mixin.json")
                || lower.endsWith(".refmap.json")
                || lower.contains("/mixin/")
                || lower.contains("/mixins/");
    }

    private static boolean isReForgedDiscoveryDescriptor(JarFile jar, JarEntry entry) {
        try (InputStream is = jar.getInputStream(entry)) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return content.contains(ModDescriptorConverter.REFORGED_DISCOVERY_MARKER);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] sanitizeManifest(byte[] input) {
        try {
            Manifest manifest = new Manifest(new ByteArrayInputStream(input));
            Attributes attrs = manifest.getMainAttributes();
            attrs.remove(new Attributes.Name("MixinConfigs"));
            attrs.remove(new Attributes.Name("TweakClass"));
            attrs.remove(new Attributes.Name("TweakOrder"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            manifest.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            warn("[ReForged] Failed to sanitize NeoForge jar manifest; copying original manifest", e);
            return input;
        }
    }
}
