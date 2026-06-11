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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 *
 * <p>Since the placeholder jar is the only part of a NeoForge mod that lives on
 * the game layer (TRANSFORMER classloader), it now also carries the mod's own
 * Mixin payload — configs, mixin classes and their reference closure — so that
 * Sponge Mixin can patch vanilla classes on behalf of the NeoForge mod (see
 * {@link NeoMixinExtractor}).</p>
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

        Set<Path> recovered = cleanStaleArtifacts(modsDir);

        int count = recovered.size();
        try (var stream = Files.list(modsDir)) {
            for (Path jar : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                if (recovered.contains(jar.toAbsolutePath().normalize())) continue;
                if (patchIfNeeded(jar)) count++;
            }
        } catch (Exception e) {
            warn("[ReForged] Error scanning mods directory: " + modsDir, e);
        }
        return count;
    }

    /**
     * Recover from interrupted patch runs:
     * <ul>
     *   <li>delete leftover {@code *.jar.tmp} files,</li>
     *   <li>re-create any {@code X.jar} whose {@code X.jar.neoforge-original}
     *       backup exists but whose placeholder vanished (a previous run was
     *       killed between writing the tmp file and the final move).</li>
     * </ul>
     */
    private static Set<Path> cleanStaleArtifacts(Path modsDir) {
        Set<Path> recovered = new HashSet<>();
        try (var stream = Files.list(modsDir)) {
            for (Path path : stream.toList()) {
                String name = path.getFileName().toString();
                if (name.endsWith(".jar.tmp")) {
                    try {
                        Files.deleteIfExists(path);
                        log("[ReForged] Removed stale temp file: " + name);
                    } catch (Exception e) {
                        warn("[ReForged] Could not remove stale temp file " + name, e);
                    }
                } else if (name.endsWith(".jar.neoforge-original")) {
                    Path baseJar = path.resolveSibling(
                            name.substring(0, name.length() - ".neoforge-original".length()));
                    if (!Files.exists(baseJar)) {
                        log("[ReForged] Restoring missing mod jar from backup: " + baseJar.getFileName());
                        // patchIfNeeded() reads from the backup when present and
                        // recreates the placeholder at the base path.
                        if (patchIfNeeded(baseJar)) {
                            recovered.add(baseJar.toAbsolutePath().normalize());
                        }
                    }
                }
            }
        } catch (Exception e) {
            warn("[ReForged] Stale artifact cleanup failed in " + modsDir, e);
        }
        return recovered;
    }

    public static boolean patchIfNeeded(Path jarPath) {
        Path backup = jarPath.resolveSibling(jarPath.getFileName() + ".neoforge-original");
        Path sourcePath = Files.exists(backup) ? backup : jarPath;
        if (!Files.exists(sourcePath)) return false;

        String neoContent;
        byte[][] entryData;
        String[] entryNames;
        boolean hasPackMeta = false;
        NeoMixinExtractor.Result mixinPayload;

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

            // Extract the mod's own mixin payload (configs + class closure) so it
            // can be applied by Forge's Mixin environment via the placeholder jar.
            NeoMixinExtractor.Result payload;
            try {
                payload = NeoMixinExtractor.extract(jar, neoContent,
                        msg -> log("[ReForged] [" + jarPath.getFileName() + "] " + msg));
            } catch (Throwable t) {
                warn("[ReForged] Mixin extraction failed for " + jarPath.getFileName()
                        + " — continuing without mixin payload", t);
                payload = new NeoMixinExtractor.Result(java.util.Map.of(), java.util.Map.of(),
                        java.util.Map.of(), List.of());
            }
            mixinPayload = payload;

            var entries = jar.entries();
            var nameList = new ArrayList<String>();
            var dataList = new ArrayList<byte[]>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (shouldDropFromForgePlaceholder(entryName)) {
                    continue;
                }
                // Mixin payload entries are written separately with rewritten content.
                if (mixinPayload.mixinConfigs().containsKey(entryName)
                        || mixinPayload.classFiles().containsKey(entryName)
                        || mixinPayload.extraResources().containsKey(entryName)) {
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
                        bytes = sanitizeManifest(bytes, mixinPayload.configNames());
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
        log("[ReForged] Patching NeoForge mod: " + jarPath.getFileName()
                + (mixinPayload.isEmpty() ? "" : " (+mixin payload)"));

        Path tempJar = jarPath.resolveSibling(jarPath.getFileName() + ".tmp");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(tempJar.toFile()))) {
            boolean wroteManifest = false;
            for (int i = 0; i < entryNames.length; i++) {
                if ("META-INF/MANIFEST.MF".equalsIgnoreCase(entryNames[i])) {
                    wroteManifest = true;
                }
                out.putNextEntry(new JarEntry(entryNames[i]));
                if (entryData[i].length > 0) {
                    out.write(entryData[i]);
                }
                out.closeEntry();
            }

            // Jars without a manifest still need one if a mixin payload exists,
            // because Mixin discovers configs via the MixinConfigs attribute.
            if (!wroteManifest && !mixinPayload.isEmpty()) {
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().putValue("MixinConfigs",
                        String.join(",", mixinPayload.configNames()));
                out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                manifest.write(out);
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

            // ── Mixin payload ──
            for (var entry : mixinPayload.mixinConfigs().entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
            for (var entry : mixinPayload.classFiles().entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
            for (var entry : mixinPayload.extraResources().entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue());
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
        if (lower.startsWith("meta-inf/services/")) {
            // Keep ordinary ServiceLoader declarations so placeholder-side mod
            // classes (mixin closure) can self-initialize on the TRANSFORMER
            // loader. Drop launch-level services that would let the mod hook
            // into ModLauncher/Mixin bootstrap.
            return lower.startsWith("meta-inf/services/cpw.mods.")
                    || lower.startsWith("meta-inf/services/org.spongepowered.")
                    || lower.startsWith("meta-inf/services/net.minecraftforge.")
                    || lower.startsWith("meta-inf/services/net.neoforged.neoforgespi.")
                    || lower.startsWith("meta-inf/services/javax.annotation.");
        }
        return "meta-inf/mods.toml".equals(lower)
                || lower.endsWith(".class")
                || lower.startsWith("meta-inf/jarjar/")
                || lower.startsWith("meta-inf/accesstransformer")
                || lower.endsWith(".mixins.json")
                || lower.endsWith(".mixin.json")
                || lower.endsWith(".refmap.json");
    }

    private static boolean isReForgedDiscoveryDescriptor(JarFile jar, JarEntry entry) {
        try (InputStream is = jar.getInputStream(entry)) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return content.contains(ModDescriptorConverter.REFORGED_DISCOVERY_MARKER);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] sanitizeManifest(byte[] input, List<String> mixinConfigNames) {
        try {
            Manifest manifest = new Manifest(new ByteArrayInputStream(input));
            Attributes attrs = manifest.getMainAttributes();
            attrs.remove(new Attributes.Name("MixinConfigs"));
            attrs.remove(new Attributes.Name("TweakClass"));
            attrs.remove(new Attributes.Name("TweakOrder"));
            if (mixinConfigNames != null && !mixinConfigNames.isEmpty()) {
                attrs.putValue("MixinConfigs", String.join(",", mixinConfigNames));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            manifest.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            warn("[ReForged] Failed to sanitize NeoForge jar manifest; copying original manifest", e);
            return input;
        }
    }
}
