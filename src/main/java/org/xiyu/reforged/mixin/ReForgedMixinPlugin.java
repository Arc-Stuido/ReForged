package org.xiyu.reforged.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Conditionally applies Mixins that depend on optional NeoForge mods.
 * <p>
 * FontJadePatchMixin requires the Jade mod to be present (its JadeFont interface
 * must be on the classpath). If Jade is not installed, the Mixin is skipped.
 * <p>
 * BalmEntityMixin is skipped when Balm is installed, because Balm's own mixin
 * already adds the same methods to Entity.
 * <p>
 * After applying FontJadePatchMixin, the plugin adds {@code snownee.jade.gui.JadeFont}
 * interface to Font's class node via ASM, avoiding compile-time dependency on Jade.
 */
public class ReForgedMixinPlugin implements IMixinConfigPlugin {

    private boolean jadePresent;
    private boolean balmPresent;

    @Override
    public void onLoad(String mixinPackage) {
        // Detect mods via Forge's mod list — class loading is unreliable at mixin init time
        jadePresent = isModLoaded("jade") || isNeoForgeModOnClasspath("jade");
        balmPresent = isModLoaded("balm") || isNeoForgeModOnClasspath("balm");
    }

    /**
     * Check if a mod is present using Forge's FMLLoader mod discovery.
     * At mixin plugin init time, mod classes are NOT on the classpath yet,
     * so Class.forName() doesn't work. Instead we query the mod list.
     */
    private static boolean isModLoaded(String modId) {
        try {
            return FMLLoader.getLoadingModList().getMods().stream()
                    .anyMatch(info -> modId.equals(info.getModId()));
        } catch (Exception e) {
            // FMLLoader may not be ready yet in very early init — fall back to false
            return false;
        }
    }

    private static boolean isNeoForgeModOnClasspath(String modId) {
        String classpath = System.getProperty("java.class.path", "");
        if (classpath.isEmpty()) return false;

        String marker = "modId=\"" + modId + "\"";
        String spacedMarker = "modId = \"" + modId + "\"";
        String[] entries = classpath.split(System.getProperty("path.separator", ":"));
        for (String entry : entries) {
            try {
                Path path = Path.of(entry);
                if (!Files.isRegularFile(path) || !entry.endsWith(".jar")) continue;

                try (JarFile jar = new JarFile(path.toFile())) {
                    var toml = jar.getEntry("META-INF/neoforge.mods.toml");
                    if (toml == null) continue;

                    try (var in = jar.getInputStream(toml)) {
                        String content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        if (content.contains(marker) || content.contains(spacedMarker)) {
                            return true;
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Optional-mod detection must never fail mixin bootstrap.
            }
        }
        return false;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("FontJadePatchMixin")) {
            return jadePresent;
        }
        if (mixinClassName.endsWith("JadeEntityAccessMixin")) {
            return jadePresent;
        }
        // Skip our BalmEntityMixin when Balm is present — Balm's own mixin already adds these methods
        if (mixinClassName.endsWith("BalmEntityMixin")) {
            return !balmPresent;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        // After FontJadePatchMixin adds the method implementations,
        // add the JadeFont interface to Font's class node via ASM
        if (mixinClassName.endsWith("FontJadePatchMixin") && jadePresent) {
            String jadeFontInternal = "snownee/jade/gui/JadeFont";
            if (!targetClass.interfaces.contains(jadeFontInternal)) {
                targetClass.interfaces.add(jadeFontInternal);
            }
        }
        // After JadeEntityAccessMixin adds callGetTypeName(),
        // add EntityAccess interface to Entity's class node via ASM.
        // The EntityAccess class will be resolved from the jade.neoforge module
        // in the Forge module layer — no shim needed. JPMS module resolution
        // ensures Entity (minecraft module) and Jade both see the same class.
        if (mixinClassName.endsWith("JadeEntityAccessMixin") && jadePresent) {
            String entityAccessInternal = "snownee/jade/mixin/EntityAccess";
            if (!targetClass.interfaces.contains(entityAccessInternal)) {
                targetClass.interfaces.add(entityAccessInternal);
            }
        }
    }
}
