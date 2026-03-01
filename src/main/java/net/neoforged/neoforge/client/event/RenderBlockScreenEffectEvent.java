package net.neoforged.neoforge.client.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a block screen effect is rendered (e.g. fire overlay, water overlay).
 */
public class RenderBlockScreenEffectEvent extends Event {
    private final BlockState blockState;
    private final BlockPos blockPos;

    public RenderBlockScreenEffectEvent(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    public BlockState getBlockState() { return blockState; }
    public BlockPos getBlockPos() { return blockPos; }

    public enum OverlayType {
        FIRE,
        WATER,
        BLOCK
    }
}
