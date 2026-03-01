package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonRenderer<T extends AbstractSkeleton> extends HumanoidMobRenderer<T, SkeletonModel<T>> {
    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public SkeletonRenderer(EntityRendererProvider.Context p_174380_) {
        this(p_174380_, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonRenderer(EntityRendererProvider.Context p_174382_, ModelLayerLocation p_174383_, ModelLayerLocation p_174384_, ModelLayerLocation p_174385_) {
        this(p_174382_, p_174384_, p_174385_, new SkeletonModel<>(p_174382_.bakeLayer(p_174383_)));
    }

    public SkeletonRenderer(EntityRendererProvider.Context p_331743_, ModelLayerLocation p_328810_, ModelLayerLocation p_335283_, SkeletonModel<T> p_335558_) {
        super(p_331743_, p_335558_, 0.5F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this, new SkeletonModel(p_331743_.bakeLayer(p_328810_)), new SkeletonModel(p_331743_.bakeLayer(p_335283_)), p_331743_.getModelManager()
            )
        );
    }

    public ResourceLocation getTextureLocation(T p_115941_) {
        return SKELETON_LOCATION;
    }

    protected boolean isShaking(T p_174389_) {
        return p_174389_.isShaking();
    }
}