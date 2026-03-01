package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player's client information is updated.
 */
public class ClientInformationUpdatedEvent extends Event {
    private final ServerPlayer player;

    public ClientInformationUpdatedEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}
