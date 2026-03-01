package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when all players have slept through the night.
 */
public class SleepFinishedTimeEvent extends Event {
    private final ServerLevel level;
    private long newTime;

    public SleepFinishedTimeEvent(ServerLevel level, long newTime) {
        this.level = level;
        this.newTime = newTime;
    }

    public ServerLevel getLevel() { return level; }
    public long getNewTime() { return newTime; }
    public void setTimeAddition(long newTime) { this.newTime = newTime; }
}
