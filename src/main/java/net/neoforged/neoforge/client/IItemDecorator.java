package net.neoforged.neoforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Item decorator for rendering extra overlays on specific items.
 */
public interface IItemDecorator {
    /**
     * @return true if you modified the RenderState and it needs to be reset for other decorators
     */
    boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
}
