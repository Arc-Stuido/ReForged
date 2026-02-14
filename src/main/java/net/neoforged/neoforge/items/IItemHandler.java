package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

/**
 * Stub: NeoForge's IItemHandler — core item handling interface.
 */
public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
}
