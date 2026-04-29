package org.xiyu.reforged.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
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
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private BossHealthOverlay bossOverlay;
    @Shadow @Final private DebugScreenOverlay debugOverlay;
    @Shadow @Final private SubtitleOverlay subtitleOverlay;

    @Shadow private void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderExperienceLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderDemoOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow private void renderTabList(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }
    @Shadow public void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker) { throw new AssertionError(); }

    @Unique
    private boolean reforged$layersApplied = false;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void reforged$applyNeoForgeLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!reforged$layersApplied) {
            reforged$layersApplied = true;
            RegisterGuiLayersEvent event = RegisterGuiLayersEvent.getInstance();
            if (event != null) {
                event.applyTo(this.layers, reforged$createVanillaLayers());
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

    @Unique
    private List<RegisterGuiLayersEvent.NamedLayer> reforged$createVanillaLayers() {
        BooleanSupplier guiVisible = () -> !this.minecraft.options.hideGui;
        List<RegisterGuiLayersEvent.NamedLayer> namedLayers = new ArrayList<>();

        reforged$add(namedLayers, VanillaGuiLayers.CAMERA_OVERLAYS, this::renderCameraOverlays, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.CROSSHAIR, this::renderCrosshair, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.HOTBAR, this::renderHotbarAndDecorations, guiVisible);

        // Forge 1.21 still renders these status elements inside renderHotbarAndDecorations().
        // Add no-op anchors so NeoForge mods can order HUD layers against the expected ids.
        reforged$anchor(namedLayers, VanillaGuiLayers.JUMP_METER);
        reforged$anchor(namedLayers, VanillaGuiLayers.EXPERIENCE_BAR);
        reforged$anchor(namedLayers, VanillaGuiLayers.PLAYER_HEALTH);
        reforged$anchor(namedLayers, VanillaGuiLayers.ARMOR_LEVEL);
        reforged$anchor(namedLayers, VanillaGuiLayers.FOOD_LEVEL);
        reforged$anchor(namedLayers, VanillaGuiLayers.VEHICLE_HEALTH);
        reforged$anchor(namedLayers, VanillaGuiLayers.AIR_LEVEL);
        reforged$anchor(namedLayers, VanillaGuiLayers.SELECTED_ITEM_NAME);
        reforged$anchor(namedLayers, VanillaGuiLayers.SPECTATOR_TOOLTIP);

        reforged$add(namedLayers, VanillaGuiLayers.EXPERIENCE_LEVEL, this::renderExperienceLevel, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.EFFECTS, this::renderEffects, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.BOSS_OVERLAY, (graphics, delta) -> this.bossOverlay.render(graphics), guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.SLEEP_OVERLAY, this::renderSleepOverlay);
        reforged$add(namedLayers, VanillaGuiLayers.DEMO_OVERLAY, this::renderDemoOverlay, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.DEBUG_OVERLAY, (graphics, delta) -> {
            if (this.debugOverlay.showDebugScreen()) {
                this.debugOverlay.render(graphics);
            }
        }, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.SCOREBOARD_SIDEBAR, this::renderScoreboardSidebar, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.OVERLAY_MESSAGE, this::renderOverlayMessage, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.TITLE, this::renderTitle, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.CHAT, this::renderChat, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.TAB_LIST, this::renderTabList, guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.SUBTITLE_OVERLAY, (graphics, delta) -> this.subtitleOverlay.render(graphics), guiVisible);
        reforged$add(namedLayers, VanillaGuiLayers.SAVING_INDICATOR, this::renderSavingIndicator);
        return namedLayers;
    }

    @Unique
    private static void reforged$add(List<RegisterGuiLayersEvent.NamedLayer> layers, ResourceLocation name,
                                    LayeredDraw.Layer layer) {
        layers.add(new RegisterGuiLayersEvent.NamedLayer(name, layer));
    }

    @Unique
    private static void reforged$add(List<RegisterGuiLayersEvent.NamedLayer> layers, ResourceLocation name,
                                    LayeredDraw.Layer layer, BooleanSupplier shouldRender) {
        layers.add(new RegisterGuiLayersEvent.NamedLayer(name, (graphics, delta) -> {
            if (shouldRender.getAsBoolean()) {
                layer.render(graphics, delta);
            }
        }));
    }

    @Unique
    private static void reforged$anchor(List<RegisterGuiLayersEvent.NamedLayer> layers, ResourceLocation name) {
        layers.add(new RegisterGuiLayersEvent.NamedLayer(name, (graphics, delta) -> {}));
    }
}
