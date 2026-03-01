package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Model {
    protected final Function<ResourceLocation, RenderType> renderType;

    public Model(Function<ResourceLocation, RenderType> p_103110_) {
        this.renderType = p_103110_;
    }

    public final RenderType renderType(ResourceLocation p_103120_) {
        return this.renderType.apply(p_103120_);
    }

    public abstract void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, int p_345283_);

    public final void renderToBuffer(PoseStack p_345147_, VertexConsumer p_343104_, int p_342281_, int p_344413_) {
        this.renderToBuffer(p_345147_, p_343104_, p_342281_, p_344413_, -1);
    }
}