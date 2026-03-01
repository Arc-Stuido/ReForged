package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stub: Fired when a player nocks an arrow. */
public class ArrowNockEvent extends PlayerEvent {
    private final ItemStack bow;

    public ArrowNockEvent(Player player, ItemStack bow) {
        super();
        this.bow = bow;
    }

    public ItemStack getBow() { return bow; }
}
