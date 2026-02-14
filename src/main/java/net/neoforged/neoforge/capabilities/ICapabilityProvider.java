package net.neoforged.neoforge.capabilities;

import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's ICapabilityProvider — generic functional interface for capability providers.
 */
@FunctionalInterface
public interface ICapabilityProvider<O, C extends @Nullable Object, T> {
    @Nullable
    T getCapability(O object, C context);
}
