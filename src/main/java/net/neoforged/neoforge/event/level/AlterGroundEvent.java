package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a tree's AlterGround feature alters ground blocks.
 */
public class AlterGroundEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private BlockState originalState;
    private BlockState newState;

    public AlterGroundEvent(Level level, BlockPos pos, BlockState originalState, BlockState newState) {
        this.level = level;
        this.pos = pos;
        this.originalState = originalState;
        this.newState = newState;
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getOriginalState() { return originalState; }
    public BlockState getNewState() { return newState; }
    public void setNewState(BlockState state) { this.newState = state; }
}
