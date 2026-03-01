package net.neoforged.neoforge.event;

import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a stat is awarded to a player.
 */
public class StatAwardEvent extends Event {
    private final Player player;
    private final Stat<?> stat;
    private int value;

    public StatAwardEvent(Player player, Stat<?> stat, int value) {
        this.player = player;
        this.stat = stat;
        this.value = value;
    }

    public Player getPlayer() { return player; }
    public Stat<?> getStat() { return stat; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
