package net.neoforged.neoforge.event.village;

import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub: Fired to allow mods to register villager trades.
 */
public class VillagerTradesEvent extends Event {
    private final int villagerLevel;
    private final List<Object> trades;

    public VillagerTradesEvent(int villagerLevel) {
        this.villagerLevel = villagerLevel;
        this.trades = new ArrayList<>();
    }

    public int getVillagerLevel() { return villagerLevel; }
    public List<Object> getTrades() { return trades; }
}
