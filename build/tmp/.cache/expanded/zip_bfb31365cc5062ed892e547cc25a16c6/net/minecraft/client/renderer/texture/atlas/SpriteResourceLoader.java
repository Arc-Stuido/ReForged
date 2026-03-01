package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SpriteResourceLoader {
    Logger LOGGER = LogUtils.getLogger();

    static SpriteResourceLoader create(Collection<MetadataSectionSerializer<?>> p_299052_) {
        return (p_296303_, p_296304_) -> {
            ResourceMetadata resourcemetadata;
            try {
                resourcemetadata = p_296304_.metadata().copySections(p_299052_);
            } catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", p_296303_, exception);
                return null;
            }

            NativeImage nativeimage;
            try (InputStream inputstream = p_296304_.open()) {
                nativeimage = NativeImage.read(inputstream);
            } catch (IOException ioexception) {
                LOGGER.error("Using missing texture, unable to load {}", p_296303_, ioexception);
                return null;
            }

            AnimationMetadataSection animationmetadatasection = resourcemetadata.getSection(AnimationMetadataSection.SERIALIZER)
                .orElse(AnimationMetadataSection.EMPTY);
            FrameSize framesize = animationmetadatasection.calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
            if (Mth.isMultipleOf(nativeimage.getWidth(), framesize.width()) && Mth.isMultipleOf(nativeimage.getHeight(), framesize.height())) {
                SpriteContents contents = net.minecraftforge.client.ForgeHooksClient.loadSpriteContents(p_296303_, p_296304_, framesize, nativeimage, resourcemetadata);
                if (contents != null) return contents;
                return new SpriteContents(p_296303_, framesize, nativeimage, resourcemetadata);
            } else {
                LOGGER.error(
                    "Image {} size {},{} is not multiple of frame size {},{}",
                    p_296303_,
                    nativeimage.getWidth(),
                    nativeimage.getHeight(),
                    framesize.width(),
                    framesize.height()
                );
                nativeimage.close();
                return null;
            }
        };
    }

    @Nullable
    SpriteContents loadSprite(ResourceLocation p_301190_, Resource p_298142_);
}
