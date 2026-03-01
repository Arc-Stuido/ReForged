package net.neoforged.neoforge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a level sound is played.
 */
public class PlayLevelSoundEvent extends Event {
    private final Level level;

    public PlayLevelSoundEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() { return level; }

    public static class AtPosition extends PlayLevelSoundEvent {
        private final BlockPos pos;

        public AtPosition(Level level, BlockPos pos) {
            super(level);
            this.pos = pos;
        }

        public BlockPos getPos() { return pos; }
    }

    public static class AtEntity extends PlayLevelSoundEvent {
        private final Entity entity;

        public AtEntity(Level level, Entity entity) {
            super(level);
            this.entity = entity;
        }

        public Entity getEntity() { return entity; }
    }
}
