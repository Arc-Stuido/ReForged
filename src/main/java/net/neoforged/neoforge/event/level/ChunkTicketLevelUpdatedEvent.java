package net.neoforged.neoforge.event.level;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when the ticket level for a chunk is updated.
 */
public class ChunkTicketLevelUpdatedEvent extends Event {
    private final int oldLevel;
    private final int newLevel;

    public ChunkTicketLevelUpdatedEvent(int oldLevel, int newLevel) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public int getOldTicketLevel() { return oldLevel; }
    public int getNewTicketLevel() { return newLevel; }
}
