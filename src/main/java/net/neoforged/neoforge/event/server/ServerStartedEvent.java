package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Stub: Fired after the server is fully started.
 */
public class ServerStartedEvent extends ServerLifecycleEvent {
    public ServerStartedEvent(MinecraftServer server) {
        super(server);
    }
}
