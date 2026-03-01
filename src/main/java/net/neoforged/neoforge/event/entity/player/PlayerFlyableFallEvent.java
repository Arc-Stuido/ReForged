package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player takes fall damage while flying.
 */
public class PlayerFlyableFallEvent extends Event {
    private final Player player;
    private float distance;
    private float multiplier;

    public PlayerFlyableFallEvent(Player player, float distance, float multiplier) {
        this.player = player;
        this.distance = distance;
        this.multiplier = multiplier;
    }

    public Player getEntity() { return player; }
    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
    public float getMultiplier() { return multiplier; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }
}
