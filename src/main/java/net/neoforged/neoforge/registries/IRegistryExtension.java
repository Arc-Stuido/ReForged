package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;

/**
 * Stub: Extension interface for Registry.
 */
public interface IRegistryExtension<T> {
    default Registry<T> self() {
        return (Registry<T>) this;
    }
}
