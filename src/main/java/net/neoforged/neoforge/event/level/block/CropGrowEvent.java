package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a crop grows.
 */
public abstract class CropGrowEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;

    public CropGrowEvent(Level level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }

    public static class Pre extends CropGrowEvent {
        public Pre(Level level, BlockPos pos, BlockState state) {
            super(level, pos, state);
        }
    }

    public static class Post extends CropGrowEvent {
        private final BlockState originalState;

        public Post(Level level, BlockPos pos, BlockState state, BlockState originalState) {
            super(level, pos, state);
            this.originalState = originalState;
        }

        public BlockState getOriginalState() { return originalState; }
    }
}
