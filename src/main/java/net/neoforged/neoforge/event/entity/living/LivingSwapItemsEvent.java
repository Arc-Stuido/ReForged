package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Stub: Fired when a living entity swaps items in hands.
 */
public class LivingSwapItemsEvent extends LivingEvent {
    private ItemStack itemSwappedToMainHand;
    private ItemStack itemSwappedToOffHand;

    public LivingSwapItemsEvent(LivingEntity entity, ItemStack toMainHand, ItemStack toOffHand) {
        super(entity);
        this.itemSwappedToMainHand = toMainHand;
        this.itemSwappedToOffHand = toOffHand;
    }

    public ItemStack getItemSwappedToMainHand() { return itemSwappedToMainHand; }
    public void setItemSwappedToMainHand(ItemStack stack) { this.itemSwappedToMainHand = stack; }
    public ItemStack getItemSwappedToOffHand() { return itemSwappedToOffHand; }
    public void setItemSwappedToOffHand(ItemStack stack) { this.itemSwappedToOffHand = stack; }
}
