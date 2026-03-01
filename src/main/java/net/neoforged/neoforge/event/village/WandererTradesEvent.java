package net.neoforged.neoforge.event.village;

import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub: Fired to allow mods to register wandering trader trades.
 */
public class WandererTradesEvent extends Event {
    private final List<Object> genericTrades;
    private final List<Object> rareTrades;

    public WandererTradesEvent() {
        this.genericTrades = new ArrayList<>();
        this.rareTrades = new ArrayList<>();
    }

    public List<Object> getGenericTrades() { return genericTrades; }
    public List<Object> getRareTrades() { return rareTrades; }
}
