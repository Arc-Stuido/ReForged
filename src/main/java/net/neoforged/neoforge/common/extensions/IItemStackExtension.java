package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nullable;

/**
 * Stub extension interface for ItemStack.
 */
public interface IItemStackExtension {

    @SuppressWarnings("unchecked")
    default ItemStack self() {
        return ItemStack.EMPTY;
    }

    default ItemStack getCraftingRemainingItem() {
        return ItemStack.EMPTY;
    }

    default boolean hasCraftingRemainingItem() {
        return false;
    }

    default int getBurnTime(@Nullable net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
        return -1;
    }

    default InteractionResult onItemUseFirst(UseOnContext context) {
        return InteractionResult.PASS;
    }

    default boolean canPerformAction(ItemAbility ability) {
        return false;
    }
}
