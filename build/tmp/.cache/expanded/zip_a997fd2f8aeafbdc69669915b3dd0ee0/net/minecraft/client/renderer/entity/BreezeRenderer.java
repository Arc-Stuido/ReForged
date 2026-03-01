package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeRenderer extends MobRenderer<Breeze, BreezeModel<Breeze>> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

    public BreezeRenderer(EntityRendererProvider.Context p_311628_) {
        super(p_311628_, new BreezeModel<>(p_311628_.bakeLayer(ModelLayers.BREEZE)), 0.5F);
        this.addLayer(new BreezeWindLayer(p_311628_, this));
        this.addLayer(new BreezeEyesLayer(this));
    }

    public void render(Breeze p_334455_, float p_333681_, float p_331379_, PoseStack p_332688_, MultiBufferSource p_333828_, int p_331024_) {
        BreezeModel<Breeze> breezemodel = this.getModel();
        enable(breezemodel, breezemodel.head(), breezemodel.rods());
        super.render(p_334455_, p_333681_, p_331379_, p_332688_, p_333828_, p_331024_);
    }

    public ResourceLocation getTextureLocation(Breeze p_312626_) {
        return TEXTURE_LOCATION;
    }

    public static BreezeModel<Breeze> enable(BreezeModel<Breeze> p_328756_, ModelPart... p_332502_) {
        p_328756_.head().visible = false;
        p_328756_.eyes().visible = false;
        p_328756_.rods().visible = false;
        p_328756_.wind().visible = false;

        for (ModelPart modelpart : p_332502_) {
            modelpart.visible = true;
        }

        return p_328756_;
    }
}