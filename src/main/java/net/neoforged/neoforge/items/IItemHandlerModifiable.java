package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

/**
 * Stub: NeoForge's IItemHandlerModifiable — extends IItemHandler with setStackInSlot.
 */
public interface IItemHandlerModifiable extends IItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}
