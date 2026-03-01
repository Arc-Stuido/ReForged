package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stub: Fired when a player destroys an item (breaks/uses up). */
public class PlayerDestroyItemEvent extends PlayerEvent {
    private final ItemStack original;

    public PlayerDestroyItemEvent(Player player, ItemStack original) {
        super();
        this.original = original;
    }

    public ItemStack getOriginal() { return original; }
}
