package net.neoforged.neoforge.event.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when chunk data is loaded or saved.
 */
public abstract class ChunkDataEvent extends Event {
    private final ChunkAccess chunk;

    public ChunkDataEvent(ChunkAccess chunk) {
        this.chunk = chunk;
    }

    public ChunkAccess getChunk() { return chunk; }

    public static class Load extends ChunkDataEvent {
        private final CompoundTag data;

        public Load(ChunkAccess chunk, CompoundTag data) {
            super(chunk);
            this.data = data;
        }

        public CompoundTag getData() { return data; }
    }

    public static class Save extends ChunkDataEvent {
        private final CompoundTag data;

        public Save(ChunkAccess chunk, CompoundTag data) {
            super(chunk);
            this.data = data;
        }

        public CompoundTag getData() { return data; }
    }
}
