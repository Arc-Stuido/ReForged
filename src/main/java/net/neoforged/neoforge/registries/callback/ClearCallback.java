package net.neoforged.neoforge.registries.callback;

/**
 * Stub: Callback fired when a registry is cleared.
 */
@FunctionalInterface
public interface ClearCallback<T> {
    void onClear(net.minecraft.core.Registry<T> registry);
}
