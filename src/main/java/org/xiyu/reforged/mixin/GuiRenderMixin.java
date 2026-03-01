package org.xiyu.reforged.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.common.MinecraftForge;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects RenderGuiEvent.Pre / Post firing into Gui.render().
 * <p>
 * Forge 1.21.1 removed RenderGuiEvent entirely (it was in the now-commented-out ForgeGui),
 * but NeoForge mods like Jade still listen for RenderGuiEvent.Post to render their HUD overlays.
 * This Mixin fires those events synthetically so NeoForge mods can render overlays.
 * </p>
 */
@Mixin(Gui.class)
public class GuiRenderMixin {

    /**
     * Fire RenderGuiEvent.Post after all GUI layers have been rendered.
     * This allows NeoForge mods (Jade, etc.) to render their HUD overlays.
     */
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"), remap = false)
    private void reforged$onRenderGuiPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, deltaTracker));
    }
}
