package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stub: Registry manager for NeoForge custom registries.
 */
public class RegistryManager {
    private static final Map<ResourceLocation, Registry<?>> REGISTRIES = new ConcurrentHashMap<>();

    private RegistryManager() {}

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return (Registry<T>) REGISTRIES.get(key.location());
    }

    public static <T> void registerRegistry(ResourceKey<? extends Registry<T>> key, Registry<T> registry) {
        REGISTRIES.put(key.location(), registry);
    }
}
