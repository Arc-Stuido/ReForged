package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

/** Wrapper around Forge's {@link net.minecraftforge.event.server.ServerStoppedEvent}. */
public class ServerStoppedEvent {
    private final net.minecraftforge.event.server.ServerStoppedEvent delegate;

    public ServerStoppedEvent(net.minecraftforge.event.server.ServerStoppedEvent delegate) {
        this.delegate = delegate;
    }

    public MinecraftServer getServer() { return delegate.getServer(); }
}
