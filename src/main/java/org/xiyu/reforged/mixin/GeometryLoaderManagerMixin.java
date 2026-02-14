package org.xiyu.reforged.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.GeometryLoaderManager;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;/**
 * Bridges model loader registration for NeoForge mods and remaps namespace lookups.
 *
 * <ol>
 *   <li>Redirects {@code get("neoforge:*")} to {@code get("forge:*")} so NeoForge mod models
 *       referencing loaders like {@code "neoforge:obj"} resolve correctly.</li>
 *   <li>After Forge's {@code init()} completes, fires the NeoForge
 *       {@code ModelEvent.RegisterGeometryLoaders} event so NeoForge mods can register their
 *       own custom geometry loaders (e.g. Twilight Forest's connected textures).</li>
 * </ol>
 */
@Mixin(value = GeometryLoaderManager.class, remap = false)
public class GeometryLoaderManagerMixin {

    private static final Logger REFORGED_LOGGER = LoggerFactory.getLogger("ReForged");

    @Shadow
    @Mutable
    private static ImmutableMap<ResourceLocation, IGeometryLoader<?>> LOADERS;

    /**
     * Remap "neoforge:*" namespace to "forge:*" for loader lookups.
     */
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void reforged$remapNeoforgeLoader(ResourceLocation name, CallbackInfoReturnable<IGeometryLoader<?>> cir) {
        if ("neoforge".equals(name.getNamespace())) {
            ResourceLocation forgeEquiv = ResourceLocation.fromNamespaceAndPath("forge", name.getPath());
            IGeometryLoader<?> loader = GeometryLoaderManager.get(forgeEquiv);
            if (loader != null) {
                cir.setReturnValue(loader);
            }
        }
    }

    /**
     * After Forge's init() freezes the LOADERS map, fire the NeoForge
     * RegisterGeometryLoaders event to let NeoForge mods register custom loaders,
     * then rebuild LOADERS with the additional entries.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private static void reforged$bridgeModelLoaderRegistration(CallbackInfo ci) {
        REFORGED_LOGGER.info("[ReForged] Bridging model loader registration to NeoForge mods...");
        try {
            // Create a NeoForge-typed map for the event, dispatch it
            Map<ResourceLocation, net.neoforged.neoforge.client.model.geometry.IGeometryLoader<?>> neoLoaders = new HashMap<>();
            var neoEvent = new net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders(neoLoaders);
            dispatchToNeoForgeModBus(neoEvent);

            if (!neoLoaders.isEmpty()) {
                // Wrap each NeoForge loader with error handling — if a loader fails
                // (e.g. due to missing NeoForge patches in vanilla classes), convert the
                // error to a JsonParseException so the model system treats it as a missing
                // model rather than crashing the game.
                Map<ResourceLocation, net.minecraftforge.client.model.geometry.IGeometryLoader<?>> merged = new HashMap<>(LOADERS);
                neoLoaders.forEach((key, loader) -> {
                    merged.put(key, wrapWithErrorHandling(key, loader));
                    REFORGED_LOGGER.info("[ReForged]   Registered NeoForge model loader: {}", key);
                });
                LOADERS = ImmutableMap.copyOf(merged);
                REFORGED_LOGGER.info("[ReForged] RegisterGeometryLoaders dispatched. {} new loaders registered.", neoLoaders.size());
            } else {
                REFORGED_LOGGER.info("[ReForged] RegisterGeometryLoaders dispatched. No new loaders registered.");
            }
        } catch (Throwable t) {
            REFORGED_LOGGER.error("[ReForged] Failed to bridge model loader registration", t);
        }
    }

    /**
     * Wraps a NeoForge geometry loader with error handling.
     * Catches all errors (including NoSuchMethodError from unpatched vanilla classes)
     * and converts them to JsonParseException so the model loading pipeline treats
     * the failure as a missing model instead of crashing.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static net.minecraftforge.client.model.geometry.IGeometryLoader<?> wrapWithErrorHandling(
            ResourceLocation key, net.neoforged.neoforge.client.model.geometry.IGeometryLoader<?> neoLoader) {
        return (IGeometryLoader) (JsonObject json, JsonDeserializationContext ctx) -> {
            try {
                return neoLoader.read(json, ctx);
            } catch (JsonParseException e) {
                throw e; // Let normal parse errors propagate
            } catch (Throwable t) {
                REFORGED_LOGGER.error("[ReForged] NeoForge model loader '{}' failed — model will show as missing: {}",
                        key, t.getMessage());
                throw new JsonParseException("[ReForged] NeoForge model loader '" + key + "' failed: " + t.getMessage(), t);
            }
        };
    }

    /**
     * Dispatch a NeoForge event to the Forge mod event bus via NeoForgeModLoader.
     */
    private static void dispatchToNeoForgeModBus(net.minecraftforge.eventbus.api.Event neoEvent) {
        try {
            Class<?> neoModLoader = Class.forName("org.xiyu.reforged.core.NeoForgeModLoader");
            Method dispatch = neoModLoader.getMethod("dispatchNeoForgeModEvent",
                    net.minecraftforge.eventbus.api.Event.class);
            dispatch.invoke(null, neoEvent);
        } catch (Throwable t) {
            REFORGED_LOGGER.warn("[ReForged] Could not dispatch event via NeoForgeModLoader: {}", t.getMessage());
        }
    }
}
