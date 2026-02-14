package net.neoforged.neoforge.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Special {@link DeferredHolder} for {@link Item Items} that implements {@link ItemLike}.
 *
 * @param <T> The specific {@link Item} type.
 */
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {

    protected DeferredItem(ResourceKey<Item> key) {
        super(key);
    }

    DeferredItem(RegistryObject<T> delegate) {
        super(delegate);
    }

    DeferredItem(RegistryObject<T> delegate, Supplier<? extends T> fallbackSup) {
        super(delegate, fallbackSup);
    }

    DeferredItem(ResourceLocation id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }

    /**
     * Creates a new {@link DeferredItem} targeting the {@link Item} with the specified name.
     */
    public static <T extends Item> DeferredItem<T> createItem(ResourceLocation key) {
        return createItem(ResourceKey.create(Registries.ITEM, key));
    }

    /**
     * Creates a new {@link DeferredItem} targeting the specified {@link Item}.
     */
    public static <T extends Item> DeferredItem<T> createItem(ResourceKey<Item> key) {
        return new DeferredItem<>(key);
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredItem (no fallback).
     */
    public static <T extends Item> DeferredItem<T> wrapItem(RegistryObject<T> obj) {
        return new DeferredItem<>(obj);
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredItem with a fallback supplier.
     */
    public static <T extends Item> DeferredItem<T> wrapItem(RegistryObject<T> obj, Supplier<? extends T> fallbackSup) {
        return new DeferredItem<>(obj, fallbackSup);
    }

    /**
     * Create a DeferredItem in direct mode.
     */
    public static <T extends Item> DeferredItem<T> createDirectItem(ResourceLocation id, Supplier<? extends T> supplier) {
        return new DeferredItem<>(id, supplier);
    }

    /**
     * Creates a new {@link ItemStack} with a default size of 1.
     */
    public ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Creates a new {@link ItemStack} with the given size.
     */
    public ItemStack toStack(int count) {
        ItemStack stack = asItem().getDefaultInstance();
        if (stack.isEmpty())
            throw new IllegalStateException("Obtained empty item stack; incorrect getDefaultInstance() call?");
        stack.setCount(count);
        return stack;
    }

    @Override
    public Item asItem() {
        return get();
    }
}
