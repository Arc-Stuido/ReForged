package net.neoforged.neoforge.common.util;

import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub: A map that uses ItemStack as keys with proper comparison.
 */
public class ItemStackMap<V> extends HashMap<ItemStack, V> {
    public ItemStackMap() {
        super();
    }

    public ItemStackMap(Map<? extends ItemStack, ? extends V> m) {
        super(m);
    }
}
