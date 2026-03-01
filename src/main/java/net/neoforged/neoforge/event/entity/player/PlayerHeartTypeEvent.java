package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to determine the player heart type for rendering.
 */
public class PlayerHeartTypeEvent extends Event {
    private final Player player;

    public PlayerHeartTypeEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
}
