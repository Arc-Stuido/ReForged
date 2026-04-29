package net.neoforged.neoforge.client.event;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.slf4j.Logger;
import org.xiyu.reforged.shim.NeoForgeShim;

/**
 * NeoForge-compatible GUI layer registration.
 *
 * <p>Forge 1.21 keeps the HUD in an unnamed {@link LayeredDraw}. NeoForge names
 * those vanilla layers and lets mods insert, replace or wrap layers by id. This
 * shim records mod operations during client setup, then resolves them against a
 * named vanilla layer list supplied by {@code GuiRenderMixin} on first HUD
 * render.</p>
 */
public class RegisterGuiLayersEvent extends net.minecraftforge.eventbus.api.Event implements IModBusEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record NamedLayer(ResourceLocation name, LayeredDraw.Layer layer) {}

    private enum Ordering {
        BEFORE, AFTER
    }

    private interface LayerOperation {
        void apply(List<NamedLayer> layers);
    }

    /** Singleton instance for deferred layer application. */
    private static volatile RegisterGuiLayersEvent instance;
    private static final Map<ResourceLocation, Long> SUSPENDED_UNTIL_NANOS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, String> LAST_FAILURES = new ConcurrentHashMap<>();
    private static final long FAILURE_BACKOFF_NANOS = 1_000_000_000L;
    private static final Field LAYERED_DRAW_LAYERS = findLayeredDrawLayersField();

    private static final List<ResourceLocation> VANILLA_LAYER_ORDER = List.of(
            VanillaGuiLayers.CAMERA_OVERLAYS,
            VanillaGuiLayers.CROSSHAIR,
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.SELECTED_ITEM_NAME,
            VanillaGuiLayers.SPECTATOR_TOOLTIP,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.EFFECTS,
            VanillaGuiLayers.BOSS_OVERLAY,
            VanillaGuiLayers.SLEEP_OVERLAY,
            VanillaGuiLayers.DEMO_OVERLAY,
            VanillaGuiLayers.DEBUG_OVERLAY,
            VanillaGuiLayers.SCOREBOARD_SIDEBAR,
            VanillaGuiLayers.OVERLAY_MESSAGE,
            VanillaGuiLayers.TITLE,
            VanillaGuiLayers.CHAT,
            VanillaGuiLayers.TAB_LIST,
            VanillaGuiLayers.SUBTITLE_OVERLAY,
            VanillaGuiLayers.SAVING_INDICATOR
    );

    private final List<LayerOperation> operations = new ArrayList<>();
    private final LinkedHashMap<ResourceLocation, LayeredDraw.Layer> registeredLayers = new LinkedHashMap<>();
    private final Set<ResourceLocation> registeredIds = new LinkedHashSet<>();

    public RegisterGuiLayersEvent() {
        instance = this;
    }

    /** Returns the most recently created event instance, or null if not yet dispatched. */
    public static RegisterGuiLayersEvent getInstance() {
        return instance;
    }

    public void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.AFTER, null, id, layer);
    }

    public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.BEFORE, null, id, layer);
    }

    public void registerAbove(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.AFTER, existingLayer, id, layer);
    }

    public void registerBelow(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.BEFORE, existingLayer, id, layer);
    }

    public void replaceLayer(ResourceLocation id, LayeredDraw.Layer replacement) {
        Objects.requireNonNull(replacement, "replacement");
        wrapLayer(id, ignored -> replacement);
    }

    public void wrapLayer(ResourceLocation id, UnaryOperator<LayeredDraw.Layer> wrapper) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(wrapper, "wrapper");
        operations.add(layers -> {
            int index = indexOf(layers, id);
            if (index < 0) {
                LOGGER.debug("[ReForged] Ignoring NeoForge GUI layer wrap for unknown id '{}'", id);
                return;
            }
            LayeredDraw.Layer wrapped = wrapper.apply(layers.get(index).layer());
            if (wrapped != null) {
                layers.set(index, new NamedLayer(id, wrapped));
            }
        });
    }

    /**
     * Returns the layers registered by NeoForge mods. This is primarily used for
     * setup diagnostics; final ordering is resolved in {@link #applyTo}.
     */
    public List<LayeredDraw.Layer> getOrderedLayers() {
        return new ArrayList<>(registeredLayers.values());
    }

    public void applyTo(LayeredDraw draw) {
        applyTo(draw, inferNamedLayers(draw));
    }

    public void applyTo(LayeredDraw draw, List<NamedLayer> vanillaLayers) {
        List<NamedLayer> resolved = new ArrayList<>(vanillaLayers);
        for (LayerOperation operation : operations) {
            operation.apply(resolved);
        }
        replaceLayers(draw, resolved);
    }

    private void register(Ordering ordering, ResourceLocation other, ResourceLocation id, LayeredDraw.Layer layer) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(layer, "layer");
        if (registeredIds.contains(id) || VANILLA_LAYER_ORDER.contains(id)) {
            throw new IllegalArgumentException("Layer already registered: " + id);
        }
        registeredIds.add(id);
        registeredLayers.put(id, layer);
        operations.add(layers -> {
            int insertPosition;
            if (other == null) {
                insertPosition = ordering == Ordering.BEFORE ? 0 : layers.size();
            } else {
                int otherIndex = indexOf(layers, other);
                if (otherIndex < 0) {
                    LOGGER.debug("[ReForged] NeoForge GUI layer '{}' ordered against unknown layer '{}'; appending",
                            id, other);
                    insertPosition = layers.size();
                } else {
                    insertPosition = otherIndex + (ordering == Ordering.BEFORE ? 0 : 1);
                }
            }
            layers.add(insertPosition, new NamedLayer(id, layer));
        });
    }

    private static int indexOf(List<NamedLayer> layers, ResourceLocation id) {
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).name().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static List<NamedLayer> inferNamedLayers(LayeredDraw draw) {
        List<LayeredDraw.Layer> raw = getRawLayers(draw);
        List<NamedLayer> named = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            ResourceLocation name = i < VANILLA_LAYER_ORDER.size()
                    ? VANILLA_LAYER_ORDER.get(i)
                    : ResourceLocation.fromNamespaceAndPath("reforged", "vanilla_layer_" + i);
            named.add(new NamedLayer(name, raw.get(i)));
        }
        return named;
    }

    @SuppressWarnings("unchecked")
    private static List<LayeredDraw.Layer> getRawLayers(LayeredDraw draw) {
        if (LAYERED_DRAW_LAYERS == null) {
            return new ArrayList<>();
        }
        try {
            return (List<LayeredDraw.Layer>) LAYERED_DRAW_LAYERS.get(draw);
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] Could not access LayeredDraw layers: {}", t.toString());
            return new ArrayList<>();
        }
    }

    private static void replaceLayers(LayeredDraw draw, List<NamedLayer> namedLayers) {
        List<LayeredDraw.Layer> raw = getRawLayers(draw);
        if (raw.isEmpty() && LAYERED_DRAW_LAYERS == null) {
            for (NamedLayer namedLayer : namedLayers) {
                draw.add(wrapLayer(namedLayer.name(), namedLayer.layer()));
            }
            return;
        }

        raw.clear();
        for (NamedLayer namedLayer : namedLayers) {
            raw.add(wrapLayer(namedLayer.name(), namedLayer.layer()));
        }
    }

    private static LayeredDraw.Layer wrapLayer(ResourceLocation name, LayeredDraw.Layer layer) {
        return (guiGraphics, partialTick) -> {
            long now = System.nanoTime();
            Long suspendedUntil = SUSPENDED_UNTIL_NANOS.get(name);
            if (suspendedUntil != null && suspendedUntil > now) {
                return;
            }
            if (suspendedUntil != null) {
                SUSPENDED_UNTIL_NANOS.remove(name, suspendedUntil);
            }

            RenderGuiLayerEvent.Pre pre = new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, name, layer);
            NeoForgeShim.EVENT_BUS.post(pre);
            if (pre.isCanceled()) {
                return;
            }

            try {
                layer.render(guiGraphics, partialTick);
                NeoForgeShim.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, partialTick, name, layer));
            } catch (Throwable t) {
                SUSPENDED_UNTIL_NANOS.put(name, System.nanoTime() + FAILURE_BACKOFF_NANOS);
                String failureKey = t.getClass().getName() + ":" + String.valueOf(t.getMessage());
                String previous = LAST_FAILURES.put(name, failureKey);
                if (!failureKey.equals(previous)) {
                    LOGGER.warn("[ReForged] Temporarily suspended NeoForge GUI layer '{}' after render failure; it will retry: {}",
                            name, t.toString(), t);
                } else {
                    LOGGER.debug("[ReForged] Temporarily suspended NeoForge GUI layer '{}' after repeated render failure: {}",
                            name, t.toString());
                }
            }
        };
    }

    private static Field findLayeredDrawLayersField() {
        try {
            Field field = LayeredDraw.class.getDeclaredField("layers");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] LayeredDraw.layers is not reflectively available: {}", t.toString());
            return null;
        }
    }
}
