package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player trades with a villager.
 */
public class TradeWithVillagerEvent extends Event {
    private final Player player;
    private final AbstractVillager villager;

    public TradeWithVillagerEvent(Player player, AbstractVillager villager) {
        this.player = player;
        this.villager = villager;
    }

    public Player getPlayer() { return player; }
    public AbstractVillager getAbstractVillager() { return villager; }
}
