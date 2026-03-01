package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player's permissions change.
 */
public class PermissionsChangedEvent extends Event {
    private final ServerPlayer player;

    public PermissionsChangedEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}
