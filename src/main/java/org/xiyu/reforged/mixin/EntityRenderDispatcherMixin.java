package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Prevents NPE when an entity has no registered renderer,
 * and injects Create's {@code create$getRenderers()} accessor method.
 * The accessor interface cast is handled by BytecodeRewriter (INVOKEINTERFACE → INVOKEVIRTUAL).
 */
@Mixin(value = EntityRenderDispatcher.class, remap = false)
public abstract class EntityRenderDispatcherMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<Deque<Integer>> REFORGED_POSE_DEPTHS =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static int reforged$poseRestoreLogCount;

    @Shadow @Final
    private Map<EntityType<?>, EntityRenderer<?>> renderers;

    /**
     * Accessor method injected into EntityRenderDispatcher.
     * Create's code calls this via INVOKEVIRTUAL after BytecodeRewriter transforms
     * the original INVOKEINTERFACE on EntityRenderDispatcherAccessor.
     */
    public Map<EntityType<?>, EntityRenderer<?>> create$getRenderers() {
        return this.renderers;
    }

    @Inject(
        method = "shouldRender",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private <E extends Entity> void reforged$skipNullRenderer(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (((EntityRenderDispatcher) (Object) this).getRenderer(entity) == null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            remap = false
    )
    private <E extends Entity> void reforged$capturePoseDepth(E entity, double x, double y, double z,
                                                              float rotationYaw, float partialTick,
                                                              PoseStack poseStack, MultiBufferSource bufferSource,
                                                              int packedLight, CallbackInfo ci) {
        REFORGED_POSE_DEPTHS.get().push(reforged$getPoseStackDepth(poseStack));
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"),
            remap = false
    )
    private <E extends Entity> void reforged$restorePoseDepth(E entity, double x, double y, double z,
                                                              float rotationYaw, float partialTick,
                                                              PoseStack poseStack, MultiBufferSource bufferSource,
                                                              int packedLight, CallbackInfo ci) {
        Deque<Integer> depths = REFORGED_POSE_DEPTHS.get();
        if (depths.isEmpty()) return;
        int target = depths.pop();
        int restored = reforged$restorePoseStackDepth(poseStack, target);
        if (restored > 0 && reforged$poseRestoreLogCount++ < 8) {
            REFORGED_LOGGER.warn("[ReForged] Restored {} leaked PoseStack frame(s) after rendering entity {} ({})",
                    restored, entity.getType(), entity.getClass().getName());
        }
        if (depths.isEmpty()) {
            REFORGED_POSE_DEPTHS.remove();
        }
    }

    private static int reforged$restorePoseStackDepth(PoseStack poseStack, int target) {
        int current = reforged$getPoseStackDepth(poseStack);
        int restored = 0;
        while (current > target) {
            try {
                poseStack.popPose();
            } catch (Throwable ignored) {
                break;
            }
            current--;
            restored++;
        }
        return restored;
    }

    private static int reforged$getPoseStackDepth(PoseStack poseStack) {
        try {
            for (java.lang.reflect.Field field : PoseStack.class.getDeclaredFields()) {
                if (java.util.Deque.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return ((java.util.Deque<?>) field.get(poseStack)).size();
                }
            }
        } catch (Throwable ignored) {}
        return 0;
    }
}
