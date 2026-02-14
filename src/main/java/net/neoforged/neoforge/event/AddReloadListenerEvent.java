package net.neoforged.neoforge.event;

import net.minecraft.server.packs.resources.PreparableReloadListener;

/** Wrapper around Forge's {@link net.minecraftforge.event.AddReloadListenerEvent}. */
public class AddReloadListenerEvent {
    private final net.minecraftforge.event.AddReloadListenerEvent delegate;

    public AddReloadListenerEvent(net.minecraftforge.event.AddReloadListenerEvent delegate) {
        this.delegate = delegate;
    }

    public void addListener(PreparableReloadListener listener) {
        delegate.addListener(listener);
    }
}
