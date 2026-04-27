package org.xiyu.reforged.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.xiyu.reforged.shim.NeoForgeShim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into Gui.render() to:
 * <ul>
 *   <li>Apply deferred NeoForge GUI layers (from {@link RegisterGuiLayersEvent}) on first render</li>
 *   <li>Fire {@link RenderGuiEvent.Post} so NeoForge mods like Jade can render HUD overlays</li>
 * </ul>
 */
@Mixin(value = Gui.class, remap = false)
public class GuiRenderMixin {

    @Shadow @Final private LayeredDraw layers;

    @Unique
    private boolean reforged$layersApplied = false;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void reforged$applyNeoForgeLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!reforged$layersApplied) {
            reforged$layersApplied = true;
            RegisterGuiLayersEvent event = RegisterGuiLayersEvent.getInstance();
            if (event != null) {
                event.applyTo(this.layers);
            }
        }

        RenderGuiEvent.Pre event = new RenderGuiEvent.Pre(guiGraphics, deltaTracker);
        postGuiEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * Fire RenderGuiEvent.Post after all GUI layers have been rendered.
     * This allows NeoForge mods (Jade, Create, etc.) to render their HUD overlays.
     */

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"), remap = false)
    private void reforged$onRenderGuiPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        postGuiEvent(new RenderGuiEvent.Post(guiGraphics, deltaTracker));
    }

    @Unique
    private static void postGuiEvent(net.minecraftforge.eventbus.api.Event event) {
        try {
            NeoForgeShim.EVENT_BUS.post(event);
        } catch (Throwable ignored) {
        }
    }
}
