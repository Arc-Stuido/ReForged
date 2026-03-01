package net.neoforged.neoforge.client.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Stub: Fired to add attribute tooltips to an item's tooltip.
 */
public class AddAttributeTooltipsEvent extends Event {
    private final ItemStack stack;

    public AddAttributeTooltipsEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() { return stack; }
}
