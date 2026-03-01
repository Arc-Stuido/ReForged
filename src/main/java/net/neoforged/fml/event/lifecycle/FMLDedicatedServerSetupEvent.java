package net.neoforged.fml.event.lifecycle;

import net.neoforged.fml.event.IModBusEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent}.
 * Dedicated-server-side setup event.
 */
public class FMLDedicatedServerSetupEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public FMLDedicatedServerSetupEvent(net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent delegate) {
        this.delegate = delegate;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return delegate.enqueueWork(work);
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
        return delegate.enqueueWork(work);
    }
}
