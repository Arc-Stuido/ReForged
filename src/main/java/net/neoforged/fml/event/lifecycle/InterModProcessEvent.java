package net.neoforged.fml.event.lifecycle;

import net.neoforged.fml.event.IModBusEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.fml.event.lifecycle.InterModProcessEvent}.
 * Process received InterModComms messages during this event.
 */
public class InterModProcessEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.fml.event.lifecycle.InterModProcessEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public InterModProcessEvent(net.minecraftforge.fml.event.lifecycle.InterModProcessEvent delegate) {
        this.delegate = delegate;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return delegate.enqueueWork(work);
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
        return delegate.enqueueWork(work);
    }
}
