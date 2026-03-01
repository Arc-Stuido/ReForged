package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired during network negotiation for a player.
 */
public class PlayerNegotiationEvent extends Event {
    private final ServerPlayer player;

    public PlayerNegotiationEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}
