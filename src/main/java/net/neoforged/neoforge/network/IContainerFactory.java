package net.neoforged.neoforge.network;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Stub: Factory interface for creating container menus from network data.
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory inv, FriendlyByteBuf data);
}
