package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindChargeRenderer extends EntityRenderer<AbstractWindCharge> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = Mth.square(3.5F);
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/wind_charge.png");
    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context p_311606_) {
        super(p_311606_);
        this.model = new WindChargeModel(p_311606_.bakeLayer(ModelLayers.WIND_CHARGE));
    }

    public void render(AbstractWindCharge p_333954_, float p_311455_, float p_312733_, PoseStack p_311350_, MultiBufferSource p_310553_, int p_310341_) {
        if (p_333954_.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(p_333954_) < (double)MIN_CAMERA_DISTANCE_SQUARED)) {
            float f = (float)p_333954_.tickCount + p_312733_;
            VertexConsumer vertexconsumer = p_310553_.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(f) % 1.0F, 0.0F));
            this.model.setupAnim(p_333954_, 0.0F, 0.0F, f, 0.0F, 0.0F);
            this.model.renderToBuffer(p_311350_, vertexconsumer, p_310341_, OverlayTexture.NO_OVERLAY);
            super.render(p_333954_, p_311455_, p_312733_, p_311350_, p_310553_, p_310341_);
        }
    }

    protected float xOffset(float p_311672_) {
        return p_311672_ * 0.03F;
    }

    public ResourceLocation getTextureLocation(AbstractWindCharge p_328306_) {
        return TEXTURE_LOCATION;
    }
}