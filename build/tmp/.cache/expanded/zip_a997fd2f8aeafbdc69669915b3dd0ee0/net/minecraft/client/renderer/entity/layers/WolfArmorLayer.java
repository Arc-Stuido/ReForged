package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfArmorLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
    private final WolfModel<Wolf> model;
    private static final Map<Crackiness.Level, ResourceLocation> ARMOR_CRACK_LOCATIONS = Map.of(
        Crackiness.Level.LOW,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_low.png"),
        Crackiness.Level.MEDIUM,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_medium.png"),
        Crackiness.Level.HIGH,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_high.png")
    );

    public WolfArmorLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> p_329010_, EntityModelSet p_329062_) {
        super(p_329010_);
        this.model = new WolfModel<>(p_329062_.bakeLayer(ModelLayers.WOLF_ARMOR));
    }

    public void render(
        PoseStack p_331942_,
        MultiBufferSource p_332785_,
        int p_336082_,
        Wolf p_327877_,
        float p_332161_,
        float p_333130_,
        float p_333869_,
        float p_332527_,
        float p_334109_,
        float p_331749_
    ) {
        if (p_327877_.hasArmor()) {
            ItemStack itemstack = p_327877_.getBodyArmorItem();
            if (itemstack.getItem() instanceof AnimalArmorItem animalarmoritem && animalarmoritem.getBodyType() == AnimalArmorItem.BodyType.CANINE) {
                this.getParentModel().copyPropertiesTo(this.model);
                this.model.prepareMobModel(p_327877_, p_332161_, p_333130_, p_333869_);
                this.model.setupAnim(p_327877_, p_332161_, p_333130_, p_332527_, p_334109_, p_331749_);
                VertexConsumer vertexconsumer = p_332785_.getBuffer(RenderType.entityCutoutNoCull(animalarmoritem.getTexture()));
                this.model.renderToBuffer(p_331942_, vertexconsumer, p_336082_, OverlayTexture.NO_OVERLAY);
                this.maybeRenderColoredLayer(p_331942_, p_332785_, p_336082_, itemstack, animalarmoritem);
                this.maybeRenderCracks(p_331942_, p_332785_, p_336082_, itemstack);
                return;
            }
        }
    }

    private void maybeRenderColoredLayer(PoseStack p_332352_, MultiBufferSource p_333624_, int p_329264_, ItemStack p_331351_, AnimalArmorItem p_333020_) {
        if (p_331351_.is(ItemTags.DYEABLE)) {
            int i = DyedItemColor.getOrDefault(p_331351_, 0);
            if (FastColor.ARGB32.alpha(i) == 0) {
                return;
            }

            ResourceLocation resourcelocation = p_333020_.getOverlayTexture();
            if (resourcelocation == null) {
                return;
            }

            this.model
                .renderToBuffer(
                    p_332352_, p_333624_.getBuffer(RenderType.entityCutoutNoCull(resourcelocation)), p_329264_, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(i)
                );
        }
    }

    private void maybeRenderCracks(PoseStack p_332031_, MultiBufferSource p_334884_, int p_329468_, ItemStack p_332244_) {
        Crackiness.Level crackiness$level = Crackiness.WOLF_ARMOR.byDamage(p_332244_);
        if (crackiness$level != Crackiness.Level.NONE) {
            ResourceLocation resourcelocation = ARMOR_CRACK_LOCATIONS.get(crackiness$level);
            VertexConsumer vertexconsumer = p_334884_.getBuffer(RenderType.entityTranslucent(resourcelocation));
            this.model.renderToBuffer(p_332031_, vertexconsumer, p_329468_, OverlayTexture.NO_OVERLAY);
        }
    }
}