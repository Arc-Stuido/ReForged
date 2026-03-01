package net.neoforged.neoforge.registries;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;

/**
 * Stub: Base mapped registry extension for NeoForge.
 */
public class BaseMappedRegistry<T> extends MappedRegistry<T> {
    public BaseMappedRegistry(ResourceKey<? extends net.minecraft.core.Registry<T>> key, com.mojang.serialization.Lifecycle lifecycle) {
        super(key, lifecycle);
    }
}
