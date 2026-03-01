package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nullable;

/**
 * Stub extension interface for Item.
 */
public interface IItemExtension {

    default Item self() {
        return (Item) this;
    }

    default boolean onDroppedByPlayer(ItemStack item, Player player) {
        return true;
    }

    default Component getHighlightTip(ItemStack item, Component displayName) {
        return displayName;
    }

    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default boolean isPiglinCurrency(ItemStack stack) {
        return false;
    }

    default boolean makesPiglinsNeutral(ItemStack stack, @Nullable net.minecraft.world.entity.LivingEntity wearer) {
        return false;
    }

    default boolean isRepairable(ItemStack stack) {
        return false;
    }

    default boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem();
    }
}
