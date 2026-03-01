package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Stub: Fired when a piston extends or retracts.
 */
public abstract class PistonEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final Direction direction;

    public PistonEvent(Level level, BlockPos pos, Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public Direction getDirection() { return direction; }

    public PistonStructureResolver getStructureHelper() {
        return new PistonStructureResolver(level, pos, direction, true);
    }

    public static class Pre extends PistonEvent {
        public Pre(Level level, BlockPos pos, Direction direction) {
            super(level, pos, direction);
        }
    }

    public static class Post extends PistonEvent {
        public Post(Level level, BlockPos pos, Direction direction) {
            super(level, pos, direction);
        }
    }
}
