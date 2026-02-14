package net.neoforged.neoforge.items.wrapper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Proxy: NeoForge's PlayerMainInvWrapper — wraps player main inventory (without armor).
 */
public class PlayerMainInvWrapper extends RangedWrapper {
    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), 0, inv.items.size());
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack rest = super.insertItem(slot, stack, simulate);
        if (rest.getCount() != stack.getCount()) {
            ItemStack inSlot = getStackInSlot(slot);
            if (!inSlot.isEmpty()) {
                if (getInventoryPlayer().player.level().isClientSide) {
                    inSlot.setPopTime(5);
                } else if (getInventoryPlayer().player instanceof ServerPlayer) {
                    getInventoryPlayer().player.containerMenu.broadcastChanges();
                }
            }
        }
        return rest;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
