package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OminousItemSpawnerRenderer extends EntityRenderer<OminousItemSpawner> {
    private static final float ROTATION_SPEED = 40.0F;
    private static final int TICKS_SCALING = 50;
    private final ItemRenderer itemRenderer;

    protected OminousItemSpawnerRenderer(EntityRendererProvider.Context p_332134_) {
        super(p_332134_);
        this.itemRenderer = p_332134_.getItemRenderer();
    }

    public ResourceLocation getTextureLocation(OminousItemSpawner p_333379_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(OminousItemSpawner p_333343_, float p_334770_, float p_333761_, PoseStack p_330642_, MultiBufferSource p_333628_, int p_334934_) {
        ItemStack itemstack = p_333343_.getItem();
        if (!itemstack.isEmpty()) {
            p_330642_.pushPose();
            if (p_333343_.tickCount <= 50) {
                float f = Math.min((float)p_333343_.tickCount + p_333761_, 50.0F) / 50.0F;
                p_330642_.scale(f, f, f);
            }

            Level level = p_333343_.level();
            float f1 = Mth.wrapDegrees((float)(level.getGameTime() - 1L)) * 40.0F;
            float f2 = Mth.wrapDegrees((float)level.getGameTime()) * 40.0F;
            p_330642_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_333761_, f1, f2)));
            ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, p_330642_, p_333628_, 15728880, itemstack, level.random, level);
            p_330642_.popPose();
        }
    }
}