package org.xiyu.reforged.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replicates Flywheel's ClientChunkCacheMixin so visual lighting updates stay in sync.
 */
@Mixin(value = ClientChunkCache.class, remap = false)
public abstract class FlywheelClientChunkCacheMixin {

    @Shadow(remap = false) @Final
    ClientLevel level;

    @Inject(method = "onLightUpdate", at = @At("TAIL"), remap = false)
    private void reforged$flywheelOnLightUpdate(LightLayer lightLayer, SectionPos sectionPos, CallbackInfo ci) {
        try {
            FlywheelRenderBridge.onLightUpdate(this.level, sectionPos, lightLayer);
        } catch (Throwable ignored) {
        }
    }
}