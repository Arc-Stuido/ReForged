package org.xiyu.reforged.mixin;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.reforged.core.ForgeTomlConfigWrapper;
import org.xiyu.reforged.core.ModDescriptorConverter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Lets Forge's parser understand NeoForge descriptors when this mixin is applied
 * early enough. The dev/runtime patcher is the primary path for jars in mods/;
 * this remains as a non-invasive fallback for command-line early mixin loading.
 */
@Mixin(ModFileParser.class)
public abstract class ModFileInfoMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "modsTomlParser", at = @At("HEAD"), cancellable = true, remap = false)
    private static void reforged$parseNeoForgeToml(IModFile rawModFile,
                                                   CallbackInfoReturnable<IModFileInfo> cir) {
        if (!(rawModFile instanceof ModFile modFile)) {
            return;
        }

        try {
            Path forgeToml = modFile.findResource("META-INF", "mods.toml");
            if (Files.exists(forgeToml)) {
                return;
            }

            Path neoToml = modFile.findResource("META-INF", "neoforge.mods.toml");
            if (!Files.exists(neoToml)) {
                return;
            }

            String neoContent = Files.readString(neoToml, StandardCharsets.UTF_8);
            String forgeContent = ModDescriptorConverter.convertForForgeDiscovery(neoContent);
            Path generatedToml = writeGeneratedToml(modFile, forgeContent);

            FileConfig config = FileConfig.builder(generatedToml).build();
            config.load();
            config.close();

            var wrapper = new ForgeTomlConfigWrapper(config);
            var info = new ModFileInfo(modFile, wrapper, ignored -> {}, List.of());
            LOGGER.info("[ReForged] Parsed NeoForge metadata for {} through generated Forge discovery descriptor",
                    modFile.getFileName());
            cir.setReturnValue(info);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Failed to parse NeoForge metadata for {}", modFile.getFilePath(), t);
        }
    }

    private static Path writeGeneratedToml(ModFile modFile, String forgeContent) throws Exception {
        Path dir;
        try {
            dir = FMLPaths.GAMEDIR.get().resolve(".reforged").resolve("generated-mod-metadata");
        } catch (Throwable ignored) {
            dir = Path.of(System.getProperty("user.dir", ".")).resolve(".reforged").resolve("generated-mod-metadata");
        }
        Files.createDirectories(dir);

        String safeName = modFile.getFileName().replaceAll("[^A-Za-z0-9._-]", "_");
        Path generatedToml = dir.resolve(safeName + ".mods.toml");
        Files.writeString(generatedToml, forgeContent, StandardCharsets.UTF_8);
        return generatedToml;
    }
}
