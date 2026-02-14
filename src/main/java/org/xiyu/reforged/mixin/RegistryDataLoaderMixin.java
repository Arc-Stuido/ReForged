package org.xiyu.reforged.mixin;

import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * Makes RegistryDataLoader tolerant of individual parse errors in modded data packs.
 *
 * <p>Without this, NeoForge mods (e.g., Twilight Forest, Create) that use NeoForge-specific
 * worldgen features, custom registries, or extended enum values will cause a crash when
 * the player tries to create a new world. The errors are logged but the game continues
 * with partial (but functional) registries.</p>
 */
@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    private static final Logger REFORGED_LOGGER = LoggerFactory.getLogger("ReForged");

    /**
     * Redirects the {@code map.isEmpty()} check in the private {@code load()} method
     * to always return {@code true}, effectively skipping the throw when there are
     * registry loading errors. The errors are logged as warnings instead.
     */
    @Redirect(
        method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;isEmpty()Z"
        ),
        remap = false
    )
    private static boolean reforged$suppressRegistryErrors(Map<ResourceKey<?>, Exception> map) {
        if (!map.isEmpty()) {
            REFORGED_LOGGER.warn("[ReForged] Suppressing {} registry data loading error(s) to prevent crash. " +
                "Some modded worldgen features may be missing.", map.size());
            for (Map.Entry<ResourceKey<?>, Exception> entry : map.entrySet()) {
                REFORGED_LOGGER.warn("[ReForged]   - {}: {}", entry.getKey(),
                    entry.getValue().getMessage() != null ? entry.getValue().getMessage() : entry.getValue().toString());
            }
        }
        return true; // Always return true (map is "empty") → skip the throw
    }
}
