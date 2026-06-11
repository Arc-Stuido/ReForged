package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

/**
 * Stub: NeoForge's IItemHandler — core item handling interface.
 *
 * <p>Extends Forge's {@code net.minecraftforge.items.IItemHandler} so that
 * instances created by ReForged shims (e.g. {@code PlayerInvWrapper}) satisfy
 * casts in rewritten NeoForge mod bytecode, where
 * {@code net.neoforged.neoforge.items.IItemHandler} is remapped to the Forge
 * interface. The method signatures are identical on both loaders.</p>
 */
public interface IItemHandler extends net.minecraftforge.items.IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
}
