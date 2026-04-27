package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;

/**
 * Prevents block entity render errors from cascading into pose-stack corruption.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();
    private static final Set<String> LOGGED_ERRORS = ConcurrentHashMap.newKeySet();
    private static final int MAX_LOGGED = 32;

    private static final ThreadLocal<PoseStack> CURRENT_POSE_STACK = new ThreadLocal<>();
    private static final ThreadLocal<Integer> POSE_DEPTH_BEFORE = ThreadLocal.withInitial(() -> 0);

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void reforged$captureRenderContext(BlockEntity blockEntity, float partialTick,
                                               PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        try {
            if (FlywheelRenderBridge.shouldSkipBlockEntityVanillaRender(blockEntity)) {
                ci.cancel();
                return;
            }
        } catch (Throwable ignored) {}

        CURRENT_POSE_STACK.set(poseStack);
        POSE_DEPTH_BEFORE.set(getPoseStackDepth(poseStack));
    }

    @Redirect(
            method = "tryRender",
            at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"),
            remap = false
    )
    private static void reforged$suppressRenderCrash(Runnable renderAction) {
        try {
            renderAction.run();
        } catch (Throwable t) {
            restorePoseStackDepth();
            String key = t.getClass().getName() + ":" +
                    (t.getMessage() != null ? t.getMessage().substring(0, Math.min(60, t.getMessage().length())) : "");
            if (LOGGED_ERRORS.size() < MAX_LOGGED && LOGGED_ERRORS.add(key)) {
                Throwable root = t;
                while (root.getCause() != null) root = root.getCause();
                REFORGED_LOGGER.error("[ReForged] Suppressed block entity render crash ({}): {}",
                        t.getClass().getSimpleName(), t.getMessage(), root);
            }
        } finally {
            CURRENT_POSE_STACK.remove();
            POSE_DEPTH_BEFORE.remove();
        }
    }

    private static void restorePoseStackDepth() {
        PoseStack poseStack = CURRENT_POSE_STACK.get();
        if (poseStack == null) return;
        int target = POSE_DEPTH_BEFORE.get();
        int current = getPoseStackDepth(poseStack);
        while (current > target) {
            try {
                poseStack.popPose();
            } catch (Throwable ignored) {
                break;
            }
            current--;
        }
    }

    private static int getPoseStackDepth(PoseStack poseStack) {
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
