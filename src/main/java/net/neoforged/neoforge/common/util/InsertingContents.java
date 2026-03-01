package net.neoforged.neoforge.common.util;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * Stub: Contents that can be inserted into.
 */
public class InsertingContents {
    private final List<ItemStack> items;

    public InsertingContents(List<ItemStack> items) {
        this.items = items;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
