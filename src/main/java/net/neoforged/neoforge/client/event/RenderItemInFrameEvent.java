package net.neoforged.neoforge.client.event;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an item is rendered inside an item frame.
 */
public class RenderItemInFrameEvent extends Event {
    private final ItemFrame itemFrame;
    private final ItemStack itemStack;

    public RenderItemInFrameEvent(ItemFrame itemFrame, ItemStack itemStack) {
        this.itemFrame = itemFrame;
        this.itemStack = itemStack;
    }

    public ItemFrame getItemFrame() { return itemFrame; }
    public ItemStack getItemStack() { return itemStack; }
}
