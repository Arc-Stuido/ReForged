package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to allow mods to modify custom mob spawners.
 */
public class ModifyCustomSpawnersEvent extends Event {
    private final ServerLevel level;

    public ModifyCustomSpawnersEvent(ServerLevel level) {
        this.level = level;
    }

    public ServerLevel getLevel() { return level; }
}
