package org.xiyu.reforged.mixin;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

/**
 * Mixin to add a NeoForge-compatible {@code component(Supplier, Object)} overload
 * to {@link Item.Properties}. NeoForge mods (e.g. Twilight Forest) use this
 * convenience method to set data components using a {@code Supplier<DataComponentType>}.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {

    /**
     * NeoForge-style component registration with Supplier.
     * Delegates to the vanilla {@code component(DataComponentType, Object)} method.
     */
    @SuppressWarnings("unchecked")
    public <T> Item.Properties component(Supplier<? extends DataComponentType<T>> supplier, T value) {
        return ((Item.Properties) (Object) this).component(supplier.get(), value);
    }

    /**
     * NeoForge-style {@code setNoRepair()} — marks the item as non-repairable.
     * In NeoForge this sets MAX_DAMAGE to 0 or a flag; in Forge it's on Item itself.
     * We implement as a no-op returning {@code this} since Forge handles repair differently.
     */
    public Item.Properties setNoRepair() {
        return (Item.Properties) (Object) this;
    }
}
