package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a fluid attempts to create a source block.
 */
public class CreateFluidSourceEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;

    public CreateFluidSourceEvent(Level level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }
}
