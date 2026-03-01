package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import net.neoforged.fml.event.IModBusEvent;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.client.event.RegisterKeyMappingsEvent}.
 * Provides the same {@code register(KeyMapping)} API.
 */
public class RegisterKeyMappingsEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.client.event.RegisterKeyMappingsEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public RegisterKeyMappingsEvent(net.minecraftforge.client.event.RegisterKeyMappingsEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * Registers a new key mapping.
     */
    public void register(KeyMapping key) {
        delegate.register(key);
    }
}
