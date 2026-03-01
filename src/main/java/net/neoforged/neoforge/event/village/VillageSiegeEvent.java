package net.neoforged.neoforge.event.village;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

/**
 * Stub: Fired when a village siege event occurs.
 */
public class VillageSiegeEvent extends Event {
    private final ServerLevel level;
    @Nullable
    private final Player player;

    public VillageSiegeEvent(ServerLevel level, @Nullable Player player) {
        this.level = level;
        this.player = player;
    }

    public ServerLevel getLevel() { return level; }
    @Nullable
    public Player getPlayer() { return player; }
}
