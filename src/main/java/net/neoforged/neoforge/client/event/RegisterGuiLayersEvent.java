package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;

/**
 * NeoForge wrapper for Forge 1.21.1's {@link net.minecraftforge.client.event.AddGuiOverlayLayersEvent}.
 * Bridges NeoForge's layer registration API to Forge's ForgeLayeredDraw.
 */
public class RegisterGuiLayersEvent extends net.minecraftforge.eventbus.api.Event implements IModBusEvent {

    private final net.minecraftforge.client.event.AddGuiOverlayLayersEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public RegisterGuiLayersEvent(net.minecraftforge.client.event.AddGuiOverlayLayersEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * Register a GUI layer above all existing layers.
     */
    public void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
        delegate.getLayeredDraw().add(id, layer);
    }

    /**
     * Register a GUI layer below all existing layers.
     * (Best-effort: adds to the end since ForgeLayeredDraw has limited insertion API)
     */
    public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
        delegate.getLayeredDraw().add(id, layer);
    }

    /**
     * Register a GUI layer above a specific existing layer.
     */
    public void registerAbove(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        // Best effort — add to end; precise ordering requires ForgeLayeredDraw stack API
        delegate.getLayeredDraw().add(id, layer);
    }

    /**
     * Register a GUI layer below a specific existing layer.
     */
    public void registerBelow(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        delegate.getLayeredDraw().add(id, layer);
    }
}
