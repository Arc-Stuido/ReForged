package net.neoforged.neoforge.event.enchanting;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to modify the effective enchantment level on an item.
 */
public class GetEnchantmentLevelEvent extends Event {
    private final ItemStack stack;

    public GetEnchantmentLevelEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() { return stack; }
}
