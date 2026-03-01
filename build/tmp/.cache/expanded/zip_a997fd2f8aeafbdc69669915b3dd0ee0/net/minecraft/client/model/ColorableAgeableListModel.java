package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ColorableAgeableListModel<E extends Entity> extends AgeableListModel<E> {
    private int color = -1;

    public void setColor(int p_343493_) {
        this.color = p_343493_;
    }

    @Override
    public void renderToBuffer(PoseStack p_102424_, VertexConsumer p_102425_, int p_102426_, int p_102427_, int p_343771_) {
        super.renderToBuffer(p_102424_, p_102425_, p_102426_, p_102427_, FastColor.ARGB32.multiply(p_343771_, this.color));
    }
}