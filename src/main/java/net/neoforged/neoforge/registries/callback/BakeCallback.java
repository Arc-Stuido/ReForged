package net.neoforged.neoforge.registries.callback;

import net.minecraft.core.Registry;

/**
 * Callback fired when a registry is baked (frozen).
 */
@FunctionalInterface
public interface BakeCallback<T> {
    void onBake(Registry<T> registry);
}
