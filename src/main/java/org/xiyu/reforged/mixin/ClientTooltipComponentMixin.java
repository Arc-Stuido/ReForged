package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.client.gui.ClientTooltipComponentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Prevents crashes from unknown {@link TooltipComponent} types registered by NeoForge mods.
 *
 * <p>NeoForge mods may register custom tooltip component types that Forge's
 * {@link ClientTooltipComponentManager} doesn't know about, causing
 * {@code IllegalArgumentException("Unknown TooltipComponent")}. This mixin
 * returns a no-op component instead of crashing.</p>
 */
@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

    /**
     * @reason Return no-op component instead of crashing for unknown NeoForge tooltip types
     * @author ReForged
     */
    @Overwrite(remap = false)
    static ClientTooltipComponent create(TooltipComponent component) {
        if (component instanceof BundleTooltip bundleTooltip) {
            return new ClientBundleTooltip(bundleTooltip.contents());
        }
        if (component instanceof ClientActivePlayersTooltip.ActivePlayersTooltip apt) {
            return new ClientActivePlayersTooltip(apt);
        }

        // Try Forge's registered factories
        ClientTooltipComponent forgeResult = ClientTooltipComponentManager.createClientTooltipComponent(component);
        if (forgeResult != null) {
            return forgeResult;
        }

        // Return no-op instead of throwing
        Holder.LOGGER.debug("[ReForged] Unknown TooltipComponent type: {}, returning empty", component.getClass().getName());
        return Holder.EMPTY;
    }

    /**
     * Holder for static fields that can't be directly in an interface Mixin.
     */
    final class Holder {
        static final Logger LOGGER = LogManager.getLogger("ReForged");
        static final ClientTooltipComponent EMPTY = new ClientTooltipComponent() {
            @Override public int getHeight() { return 0; }
            @Override public int getWidth(net.minecraft.client.gui.Font font) { return 0; }
        };
    }
}
