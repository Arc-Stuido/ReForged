package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected static final float NAMETAG_SCALE = 0.025F;
    public static final int LEASH_RENDER_STEPS = 24;
    protected final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;

    protected EntityRenderer(EntityRendererProvider.Context p_174008_) {
        this.entityRenderDispatcher = p_174008_.getEntityRenderDispatcher();
        this.font = p_174008_.getFont();
    }

    public final int getPackedLightCoords(T p_114506_, float p_114507_) {
        BlockPos blockpos = BlockPos.containing(p_114506_.getLightProbePosition(p_114507_));
        return LightTexture.pack(this.getBlockLightLevel(p_114506_, blockpos), this.getSkyLightLevel(p_114506_, blockpos));
    }

    protected int getSkyLightLevel(T p_114509_, BlockPos p_114510_) {
        return p_114509_.level().getBrightness(LightLayer.SKY, p_114510_);
    }

    protected int getBlockLightLevel(T p_114496_, BlockPos p_114497_) {
        return p_114496_.isOnFire() ? 15 : p_114496_.level().getBrightness(LightLayer.BLOCK, p_114497_);
    }

    public boolean shouldRender(T p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        if (!p_114491_.shouldRender(p_114493_, p_114494_, p_114495_)) {
            return false;
        } else if (p_114491_.noCulling) {
            return true;
        } else {
            AABB aabb = p_114491_.getBoundingBoxForCulling().inflate(0.5);
            if (aabb.hasNaN() || aabb.getSize() == 0.0) {
                aabb = new AABB(
                    p_114491_.getX() - 2.0,
                    p_114491_.getY() - 2.0,
                    p_114491_.getZ() - 2.0,
                    p_114491_.getX() + 2.0,
                    p_114491_.getY() + 2.0,
                    p_114491_.getZ() + 2.0
                );
            }

            if (p_114492_.isVisible(aabb)) {
                return true;
            } else {
                if (p_114491_ instanceof Leashable leashable) {
                    Entity entity = leashable.getLeashHolder();
                    if (entity != null) {
                        return p_114492_.isVisible(entity.getBoundingBoxForCulling());
                    }
                }

                return false;
            }
        }
    }

    public Vec3 getRenderOffset(T p_114483_, float p_114484_) {
        return Vec3.ZERO;
    }

    public void render(T p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
        if (p_114485_ instanceof Leashable leashable) {
            Entity entity = leashable.getLeashHolder();
            if (entity != null) {
                this.renderLeash(p_114485_, p_114487_, p_114488_, p_114489_, entity);
            }
        }

        var event = net.minecraftforge.client.event.ForgeEventFactoryClient.fireRenderNameTagEvent(p_114485_, p_114485_.getDisplayName(), this, p_114488_, p_114489_, p_114490_, p_114487_);
        if (!event.getResult().isDenied() && (event.getResult().isAllowed() || this.shouldShowName(p_114485_))) {
           this.renderNameTag(p_114485_, event.getContent(), p_114488_, p_114489_, p_114490_, p_114487_);
        }
    }

    private <E extends Entity> void renderLeash(T p_343368_, float p_344915_, PoseStack p_344390_, MultiBufferSource p_342590_, E p_344166_) {
        p_344390_.pushPose();
        Vec3 vec3 = p_344166_.getRopeHoldPosition(p_344915_);
        double d0 = (double)(p_343368_.getPreciseBodyRotation(p_344915_) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
        Vec3 vec31 = p_343368_.getLeashOffset(p_344915_);
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp((double)p_344915_, p_343368_.xo, p_343368_.getX()) + d1;
        double d4 = Mth.lerp((double)p_344915_, p_343368_.yo, p_343368_.getY()) + vec31.y;
        double d5 = Mth.lerp((double)p_344915_, p_343368_.zo, p_343368_.getZ()) + d2;
        p_344390_.translate(d1, vec31.y, d2);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = p_342590_.getBuffer(RenderType.leash());
        Matrix4f matrix4f = p_344390_.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = BlockPos.containing(p_343368_.getEyePosition(p_344915_));
        BlockPos blockpos1 = BlockPos.containing(p_344166_.getEyePosition(p_344915_));
        int i = this.getBlockLightLevel(p_343368_, blockpos);
        int j = this.entityRenderDispatcher.getRenderer(p_344166_).getBlockLightLevel(p_344166_, blockpos1);
        int k = p_343368_.level().getBrightness(LightLayer.SKY, blockpos);
        int l = p_343368_.level().getBrightness(LightLayer.SKY, blockpos1);

        for (int i1 = 0; i1 <= 24; i1++) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for (int j1 = 24; j1 >= 0; j1--) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        }

        p_344390_.popPose();
    }

    private static void addVertexPair(
        VertexConsumer p_344804_,
        Matrix4f p_343855_,
        float p_342047_,
        float p_343146_,
        float p_342344_,
        int p_342780_,
        int p_343511_,
        int p_342326_,
        int p_343961_,
        float p_342941_,
        float p_343681_,
        float p_343907_,
        float p_343356_,
        int p_342821_,
        boolean p_343253_
    ) {
        float f = (float)p_342821_ / 24.0F;
        int i = (int)Mth.lerp(f, (float)p_342780_, (float)p_343511_);
        int j = (int)Mth.lerp(f, (float)p_342326_, (float)p_343961_);
        int k = LightTexture.pack(i, j);
        float f1 = p_342821_ % 2 == (p_343253_ ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = p_342047_ * f;
        float f6 = p_343146_ > 0.0F ? p_343146_ * f * f : p_343146_ - p_343146_ * (1.0F - f) * (1.0F - f);
        float f7 = p_342344_ * f;
        p_344804_.addVertex(p_343855_, f5 - p_343907_, f6 + p_343681_, f7 + p_343356_).setColor(f2, f3, f4, 1.0F).setLight(k);
        p_344804_.addVertex(p_343855_, f5 + p_343907_, f6 + p_342941_ - p_343681_, f7 - p_343356_).setColor(f2, f3, f4, 1.0F).setLight(k);
    }

    protected boolean shouldShowName(T p_114504_) {
        return p_114504_.shouldShowName() || p_114504_.hasCustomName() && p_114504_ == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public abstract ResourceLocation getTextureLocation(T p_114482_);

    public Font getFont() {
        return this.font;
    }

    protected void renderNameTag(T p_114498_, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_, float p_334448_) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(p_114498_);
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(p_114498_, d0)) {
            Vec3 vec3 = p_114498_.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, p_114498_.getViewYRot(p_334448_));
            if (vec3 != null) {
                boolean flag = !p_114498_.isDiscrete();
                int i = "deadmau5".equals(p_114499_.getString()) ? -10 : 0;
                p_114500_.pushPose();
                p_114500_.translate(vec3.x, vec3.y + 0.5, vec3.z);
                p_114500_.mulPose(this.entityRenderDispatcher.cameraOrientation());
                p_114500_.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = p_114500_.last().pose();
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int)(f * 255.0F) << 24;
                Font font = this.getFont();
                float f1 = (float)(-font.width(p_114499_) / 2);
                font.drawInBatch(
                    p_114499_, f1, (float)i, 553648127, false, matrix4f, p_114501_, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, p_114502_
                );
                if (flag) {
                    font.drawInBatch(p_114499_, f1, (float)i, -1, false, matrix4f, p_114501_, Font.DisplayMode.NORMAL, 0, p_114502_);
                }

                p_114500_.popPose();
            }
        }
    }

    protected float getShadowRadius(T p_335587_) {
        return this.shadowRadius;
    }
}
