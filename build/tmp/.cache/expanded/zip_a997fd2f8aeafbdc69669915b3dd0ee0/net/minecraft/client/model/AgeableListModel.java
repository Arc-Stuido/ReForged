package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
    public final boolean scaleHead;
    public final float babyYHeadOffset;
    public final float babyZHeadOffset;
    public final float babyHeadScale;
    private final float babyBodyScale;
    private final float bodyYOffset;

    protected AgeableListModel(boolean p_102023_, float p_102024_, float p_102025_) {
        this(p_102023_, p_102024_, p_102025_, 2.0F, 2.0F, 24.0F);
    }

    protected AgeableListModel(boolean p_102027_, float p_102028_, float p_102029_, float p_102030_, float p_102031_, float p_102032_) {
        this(RenderType::entityCutoutNoCull, p_102027_, p_102028_, p_102029_, p_102030_, p_102031_, p_102032_);
    }

    protected AgeableListModel(
        Function<ResourceLocation, RenderType> p_102015_,
        boolean p_102016_,
        float p_102017_,
        float p_102018_,
        float p_102019_,
        float p_102020_,
        float p_102021_
    ) {
        super(p_102015_);
        this.scaleHead = p_102016_;
        this.babyYHeadOffset = p_102017_;
        this.babyZHeadOffset = p_102018_;
        this.babyHeadScale = p_102019_;
        this.babyBodyScale = p_102020_;
        this.bodyYOffset = p_102021_;
    }

    protected AgeableListModel() {
        this(false, 5.0F, 2.0F);
    }

    @Override
    public void renderToBuffer(PoseStack p_102034_, VertexConsumer p_102035_, int p_102036_, int p_102037_, int p_343597_) {
        if (this.young) {
            p_102034_.pushPose();
            if (this.scaleHead) {
                float f = 1.5F / this.babyHeadScale;
                p_102034_.scale(f, f, f);
            }

            p_102034_.translate(0.0F, this.babyYHeadOffset / 16.0F, this.babyZHeadOffset / 16.0F);
            this.headParts().forEach(p_340834_ -> p_340834_.render(p_102034_, p_102035_, p_102036_, p_102037_, p_343597_));
            p_102034_.popPose();
            p_102034_.pushPose();
            float f1 = 1.0F / this.babyBodyScale;
            p_102034_.scale(f1, f1, f1);
            p_102034_.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
            this.bodyParts().forEach(p_340840_ -> p_340840_.render(p_102034_, p_102035_, p_102036_, p_102037_, p_343597_));
            p_102034_.popPose();
        } else {
            this.headParts().forEach(p_340846_ -> p_340846_.render(p_102034_, p_102035_, p_102036_, p_102037_, p_343597_));
            this.bodyParts().forEach(p_340852_ -> p_340852_.render(p_102034_, p_102035_, p_102036_, p_102037_, p_343597_));
        }
    }

    protected abstract Iterable<ModelPart> headParts();

    protected abstract Iterable<ModelPart> bodyParts();
}