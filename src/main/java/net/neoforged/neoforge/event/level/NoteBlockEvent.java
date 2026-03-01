package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a note block is played or changed.
 */
public abstract class NoteBlockEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private int noteId;

    public NoteBlockEvent(Level level, BlockPos pos, BlockState state, int note) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.noteId = note;
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }
    public int getVanillaNoteId() { return noteId; }
    public void setNote(int note) { this.noteId = note; }

    public static class Play extends NoteBlockEvent {
        public Play(Level level, BlockPos pos, BlockState state, int note) {
            super(level, pos, state, note);
        }
    }

    public static class Change extends NoteBlockEvent {
        private int oldNote;

        public Change(Level level, BlockPos pos, BlockState state, int oldNote, int newNote) {
            super(level, pos, state, newNote);
            this.oldNote = oldNote;
        }

        public int getOldNote() { return oldNote; }
    }
}
