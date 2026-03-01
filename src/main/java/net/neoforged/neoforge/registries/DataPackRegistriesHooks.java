package net.neoforged.neoforge.registries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import java.util.Set;
import java.util.Collections;

/**
 * Stub: Hooks for data pack registries.
 */
public class DataPackRegistriesHooks {
    private DataPackRegistriesHooks() {}

    public static Set<ResourceKey<? extends Registry<?>>> getDataPackRegistries() {
        return Collections.emptySet();
    }

    public static RegistryAccess.Frozen wrapRegistryAccess(RegistryAccess.Frozen registryAccess) {
        return registryAccess;
    }
}
