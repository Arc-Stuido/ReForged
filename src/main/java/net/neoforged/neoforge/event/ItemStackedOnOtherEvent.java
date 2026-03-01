package net.neoforged.neoforge.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an item stack is placed on another in a container.
 */
public class ItemStackedOnOtherEvent extends Event {
    private final ItemStack carriedItem;
    private final ItemStack stackedOnItem;

    public ItemStackedOnOtherEvent(ItemStack carriedItem, ItemStack stackedOnItem) {
        this.carriedItem = carriedItem;
        this.stackedOnItem = stackedOnItem;
    }

    public ItemStack getCarriedItem() { return carriedItem; }
    public ItemStack getStackedOnItem() { return stackedOnItem; }
}
