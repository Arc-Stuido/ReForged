package org.xiyu.reforged.mixin;

import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.reforged.bridge.BlockStateIdMapBridge;

/**
 * Mirrors NeoForge's dedicated block-state ID map behavior when Forge's slave
 * map is not available during early RegisterEvent handlers.
 */
@Mixin(value = GameData.BlockCallbacks.class, remap = false)
public abstract class GameDataBlockCallbacksMixin {

    /**
     * @author ReForged
     * @reason Forge's block-state slave map can be unavailable while NeoForge
     * RegisterEvent handlers eagerly initialize entity classes. NeoForge expects
     * a stable mapper object here, so we provide one and rebuild it after bake.
     */
    @Overwrite(remap = false)
    public static IdMapper<BlockState> getBlockStateIDMap() {
        return BlockStateIdMapBridge.fallbackBlockStateIdMap();
    }

    @Inject(method = "onClear", at = @At("TAIL"), remap = false)
    private void reforged$clearFallbackMap(IForgeRegistryInternal<Block> owner, RegistryManager stage, CallbackInfo ci) {
        BlockStateIdMapBridge.clearFallback();
    }

    @Inject(method = "onBake", at = @At("TAIL"), remap = false)
    private void reforged$rebuildFallbackMap(IForgeRegistryInternal<Block> owner, RegistryManager stage, CallbackInfo ci) {
        BlockStateIdMapBridge.rebuildFallback(owner);
    }
}
