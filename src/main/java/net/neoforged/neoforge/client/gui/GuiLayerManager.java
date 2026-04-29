package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.xiyu.reforged.shim.NeoForgeShim;

/**
 * Minimal NeoForge GuiLayerManager facade.
 *
 * <p>ReForged drives Forge's vanilla {@link LayeredDraw} through
 * {@link RegisterGuiLayersEvent}, but some NeoForge mods still link against
 * this internal manager type. Keeping the small facade avoids class-linking
 * failures without replacing the whole HUD pipeline.</p>
 */
public class GuiLayerManager {
    public static final float Z_SEPARATION = LayeredDraw.Z_SEPARATION;

    private final List<NamedLayer> layers = new ArrayList<>();
    private boolean initialized;

    public record NamedLayer(ResourceLocation name, LayeredDraw.Layer layer) {}

    public GuiLayerManager add(ResourceLocation name, LayeredDraw.Layer layer) {
        this.layers.add(new NamedLayer(name, layer));
        return this;
    }

    public GuiLayerManager add(ResourceLocation name, LayeredDraw.Layer layer, BooleanSupplier shouldRender) {
        return add(name, (graphics, delta) -> {
            if (shouldRender.getAsBoolean()) {
                layer.render(graphics, delta);
            }
        });
    }

    public GuiLayerManager add(GuiLayerManager child, BooleanSupplier shouldRender) {
        for (NamedLayer entry : child.layers) {
            add(entry.name(), (graphics, delta) -> {
                if (shouldRender.getAsBoolean()) {
                    entry.layer().render(graphics, delta);
                }
            });
        }
        return this;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        RenderGuiEvent.Pre pre = new RenderGuiEvent.Pre(guiGraphics, partialTick);
        NeoForgeShim.EVENT_BUS.post(pre);
        if (pre.isCanceled()) {
            return;
        }

        guiGraphics.pose().pushPose();
        try {
            for (NamedLayer layer : this.layers) {
                RenderGuiLayerEvent.Pre layerPre =
                        new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, layer.name(), layer.layer());
                NeoForgeShim.EVENT_BUS.post(layerPre);
                if (!layerPre.isCanceled()) {
                    layer.layer().render(guiGraphics, partialTick);
                    NeoForgeShim.EVENT_BUS.post(
                            new RenderGuiLayerEvent.Post(guiGraphics, partialTick, layer.name(), layer.layer()));
                }
                guiGraphics.pose().translate(0.0F, 0.0F, Z_SEPARATION);
            }
        } finally {
            guiGraphics.pose().popPose();
        }

        NeoForgeShim.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, partialTick));
    }

    public void initModdedLayers() {
        if (this.initialized) {
            throw new IllegalStateException("Duplicate initialization of GuiLayerManager");
        }
        this.initialized = true;
        RegisterGuiLayersEvent event = new RegisterGuiLayersEvent();
        NeoForgeShim.EVENT_BUS.post(event);
    }

    public int getLayerCount() {
        return this.layers.size();
    }

    public List<NamedLayer> getLayers() {
        return this.layers;
    }
}
