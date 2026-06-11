package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

/**
 * Stub: NeoForge's IItemHandlerModifiable — extends IItemHandler with setStackInSlot.
 *
 * <p>Also extends Forge's {@code IItemHandlerModifiable} for cross-loader cast
 * compatibility (see {@link IItemHandler}).</p>
 */
public interface IItemHandlerModifiable extends IItemHandler, net.minecraftforge.items.IItemHandlerModifiable {
    void setStackInSlot(int slot, ItemStack stack);
}
