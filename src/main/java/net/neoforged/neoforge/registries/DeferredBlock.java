package net.neoforged.neoforge.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Special {@link DeferredHolder} for {@link Block Blocks} that implements {@link ItemLike}.
 *
 * @param <T> The specific {@link Block} type.
 */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {

    protected DeferredBlock(ResourceKey<Block> key) {
        super(key);
    }

    DeferredBlock(RegistryObject<T> delegate) {
        super(delegate);
    }

    DeferredBlock(RegistryObject<T> delegate, Supplier<? extends T> fallbackSup) {
        super(delegate, fallbackSup);
    }

    DeferredBlock(ResourceLocation id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }

    /**
     * Creates a new {@link DeferredBlock} targeting the {@link Block} with the specified name.
     */
    public static <T extends Block> DeferredBlock<T> createBlock(ResourceLocation key) {
        return createBlock(ResourceKey.create(Registries.BLOCK, key));
    }

    /**
     * Creates a new {@link DeferredBlock} targeting the specified {@link Block}.
     */
    public static <T extends Block> DeferredBlock<T> createBlock(ResourceKey<Block> key) {
        return new DeferredBlock<>(key);
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredBlock.
     */
    public static <T extends Block> DeferredBlock<T> wrapBlock(RegistryObject<T> obj) {
        return new DeferredBlock<>(obj);
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
            throw new IllegalStateException("Block does not have a corresponding item: " + getId());
        stack.setCount(count);
        return stack;
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
