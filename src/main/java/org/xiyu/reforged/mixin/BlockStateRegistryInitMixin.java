package org.xiyu.reforged.mixin;

import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.xiyu.reforged.bridge.BlockStateIdMapBridge;

/**
 * Keeps Block.BLOCK_STATE_REGISTRY stable when Block is initialized during
 * early NeoForge RegisterEvent callbacks.
 */
@Mixin(Block.class)
public abstract class BlockStateRegistryInitMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/GameData$BlockCallbacks;getBlockStateIDMap()Lnet/minecraft/core/IdMapper;"
            ),
            require = 0,
            remap = false
    )
    private static IdMapper<BlockState> reforged$useForgeFallbackBlockStateIdMap() {
        return BlockStateIdMapBridge.fallbackBlockStateIdMap();
    }

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/registries/GameData;getBlockStateIDMap()Lnet/minecraft/core/IdMapper;"
            ),
            require = 0,
            remap = false
    )
    private static IdMapper<BlockState> reforged$useNeoForgeFallbackBlockStateIdMap() {
        return BlockStateIdMapBridge.fallbackBlockStateIdMap();
    }
}
