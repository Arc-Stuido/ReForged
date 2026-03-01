package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Stub: Fired when a player tosses an item.
 */
public class ItemTossEvent extends ItemEvent {
    private final Player player;

    public ItemTossEvent(ItemEntity entityItem, Player player) {
        super(entityItem);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
