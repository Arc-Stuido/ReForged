package net.neoforged.neoforge.event.brewing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player picks up a brewed potion from a brewing stand.
 */
public class PlayerBrewedPotionEvent extends Event {
    private final Player player;
    private final ItemStack stack;

    public PlayerBrewedPotionEvent(Player player, ItemStack stack) {
        this.player = player;
        this.stack = stack;
    }

    public Player getPlayer() { return player; }
    public ItemStack getStack() { return stack; }
}
