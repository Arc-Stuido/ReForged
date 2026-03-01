package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;

/** Stub: Fired when a player wakes up. */
public class PlayerWakeUpEvent extends PlayerEvent {
    private final boolean wakeImmediately;
    private final boolean updateLevel;

    public PlayerWakeUpEvent(Player player, boolean wakeImmediately, boolean updateLevel) {
        super();
        this.wakeImmediately = wakeImmediately;
        this.updateLevel = updateLevel;
    }

    public boolean wakeImmediately() { return wakeImmediately; }
    public boolean updateLevel() { return updateLevel; }
}
