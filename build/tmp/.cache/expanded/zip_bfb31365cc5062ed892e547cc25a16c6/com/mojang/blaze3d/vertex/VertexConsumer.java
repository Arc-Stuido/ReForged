package com.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public interface VertexConsumer extends net.minecraftforge.client.extensions.IForgeVertexConsumer {
    VertexConsumer addVertex(float p_344294_, float p_342213_, float p_344859_);

    VertexConsumer setColor(int p_342749_, int p_344324_, int p_343336_, int p_342831_);

    VertexConsumer setUv(float p_344155_, float p_345269_);

    VertexConsumer setUv1(int p_344168_, int p_342818_);

    VertexConsumer setUv2(int p_342773_, int p_345341_);

    VertexConsumer setNormal(float p_342733_, float p_342268_, float p_344916_);

    default void addVertex(
        float p_342335_,
        float p_342594_,
        float p_342395_,
        int p_344436_,
        float p_344317_,
        float p_344558_,
        int p_344862_,
        int p_343109_,
        float p_343232_,
        float p_342995_,
        float p_343739_
    ) {
        this.addVertex(p_342335_, p_342594_, p_342395_);
        this.setColor(p_344436_);
        this.setUv(p_344317_, p_344558_);
        this.setOverlay(p_344862_);
        this.setLight(p_343109_);
        this.setNormal(p_343232_, p_342995_, p_343739_);
    }

    default VertexConsumer setColor(float p_345344_, float p_343040_, float p_343668_, float p_342740_) {
        return this.setColor((int)(p_345344_ * 255.0F), (int)(p_343040_ * 255.0F), (int)(p_343668_ * 255.0F), (int)(p_342740_ * 255.0F));
    }

    default VertexConsumer setColor(int p_345390_) {
        return this.setColor(
            FastColor.ARGB32.red(p_345390_),
            FastColor.ARGB32.green(p_345390_),
            FastColor.ARGB32.blue(p_345390_),
            FastColor.ARGB32.alpha(p_345390_)
        );
    }

    default VertexConsumer setWhiteAlpha(int p_342254_) {
        return this.setColor(FastColor.ARGB32.color(p_342254_, -1));
    }

    default VertexConsumer setLight(int p_342385_) {
        return this.setUv2(p_342385_ & 65535, p_342385_ >> 16 & 65535);
    }

    default VertexConsumer setOverlay(int p_345433_) {
        return this.setUv1(p_345433_ & 65535, p_345433_ >> 16 & 65535);
    }

    default void putBulkData(
        PoseStack.Pose p_85996_, BakedQuad p_85997_, float p_85999_, float p_86000_, float p_86001_, float p_330684_, int p_86003_, int p_332867_
    ) {
        this.putBulkData(
            p_85996_,
            p_85997_,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            p_85999_,
            p_86000_,
            p_86001_,
            p_330684_,
            new int[]{p_86003_, p_86003_, p_86003_, p_86003_},
            p_332867_,
            false
        );
    }

    default void putBulkData(
        PoseStack.Pose p_85996_,
        BakedQuad p_85997_,
        float[] p_85998_,
        float p_85999_,
        float p_86000_,
        float p_86001_,
        float alpha,
        int[] p_86002_,
        int p_86003_,
        boolean p_86004_
    ) {
        int[] aint = p_85997_.getVertices();
        Vec3i vec3i = p_85997_.getDirection().getNormal();
        Matrix4f matrix4f = p_85996_.pose();
        Vector3f vector3f = p_85996_.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
        int i = 8;
        int j = aint.length / 8;
        int k = (int)(alpha * 255.0F);

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int l = 0; l < j; l++) {
                intbuffer.clear();
                intbuffer.put(aint, l * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (p_86004_) {
                    float f6 = (float)(bytebuffer.get(12) & 255);
                    float f7 = (float)(bytebuffer.get(13) & 255);
                    float f8 = (float)(bytebuffer.get(14) & 255);
                    f3 = f6 * p_85998_[l] * p_85999_;
                    f4 = f7 * p_85998_[l] * p_86000_;
                    f5 = f8 * p_85998_[l] * p_86001_;
                } else {
                    f3 = p_85998_[l] * p_85999_ * 255.0F;
                    f4 = p_85998_[l] * p_86000_ * 255.0F;
                    f5 = p_85998_[l] * p_86001_ * 255.0F;
                }

                int i1 = FastColor.ARGB32.color(k, (int)f3, (int)f4, (int)f5);
                int j1 = applyBakedLighting(p_86002_[l], bytebuffer);
                float f10 = bytebuffer.getFloat(16);
                float f9 = bytebuffer.getFloat(20);
                Vector3f vector3f1 = matrix4f.transformPosition(f, f1, f2, new Vector3f());
                applyBakedNormals(vector3f, bytebuffer, p_85996_.normal());
                this.addVertex(vector3f1.x(), vector3f1.y(), vector3f1.z(), i1, f10, f9, p_86003_, j1, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default VertexConsumer addVertex(Vector3f p_343309_) {
        return this.addVertex(p_343309_.x(), p_343309_.y(), p_343309_.z());
    }

    default VertexConsumer addVertex(PoseStack.Pose p_343718_, Vector3f p_344795_) {
        return this.addVertex(p_343718_, p_344795_.x(), p_344795_.y(), p_344795_.z());
    }

    default VertexConsumer addVertex(PoseStack.Pose p_343203_, float p_343315_, float p_342573_, float p_344986_) {
        return this.addVertex(p_343203_.pose(), p_343315_, p_342573_, p_344986_);
    }

    default VertexConsumer addVertex(Matrix4f p_344823_, float p_342636_, float p_342677_, float p_343814_) {
        Vector3f vector3f = p_344823_.transformPosition(p_342636_, p_342677_, p_343814_, new Vector3f());
        return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default VertexConsumer setNormal(PoseStack.Pose p_343706_, float p_345121_, float p_344892_, float p_344341_) {
        Vector3f vector3f = p_343706_.transformNormal(p_345121_, p_344892_, p_344341_, new Vector3f());
        return this.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
    }
}
