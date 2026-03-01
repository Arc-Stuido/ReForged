package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final SkeletonModel<T> layerModel;
    private final ResourceLocation clothesLocation;

    public SkeletonClothingLayer(RenderLayerParent<T, M> p_330715_, EntityModelSet p_334793_, ModelLayerLocation p_335699_, ResourceLocation p_330798_) {
        super(p_330715_);
        this.clothesLocation = p_330798_;
        this.layerModel = new SkeletonModel<>(p_334793_.bakeLayer(p_335699_));
    }

    public void render(
        PoseStack p_332269_,
        MultiBufferSource p_333438_,
        int p_331437_,
        T p_334164_,
        float p_330307_,
        float p_333019_,
        float p_334996_,
        float p_334216_,
        float p_332158_,
        float p_335772_
    ) {
        coloredCutoutModelCopyLayerRender(
            this.getParentModel(),
            this.layerModel,
            this.clothesLocation,
            p_332269_,
            p_333438_,
            p_331437_,
            p_334164_,
            p_330307_,
            p_333019_,
            p_334216_,
            p_332158_,
            p_335772_,
            p_334996_,
            -1
        );
    }
}