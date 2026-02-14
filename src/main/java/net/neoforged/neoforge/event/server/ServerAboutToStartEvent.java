package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

/** Wrapper around Forge's {@link net.minecraftforge.event.server.ServerAboutToStartEvent}. */
public class ServerAboutToStartEvent {
    private final net.minecraftforge.event.server.ServerAboutToStartEvent delegate;

    public ServerAboutToStartEvent(net.minecraftforge.event.server.ServerAboutToStartEvent delegate) {
        this.delegate = delegate;
    }

    public MinecraftServer getServer() { return delegate.getServer(); }
}
