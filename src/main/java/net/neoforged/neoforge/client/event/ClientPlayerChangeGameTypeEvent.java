package net.neoforged.neoforge.client.event;

import net.minecraft.world.level.GameType;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player's game type changes on the client.
 */
public class ClientPlayerChangeGameTypeEvent extends Event {
    private final GameType currentGameType;
    private final GameType newGameType;

    public ClientPlayerChangeGameTypeEvent(GameType current, GameType newType) {
        this.currentGameType = current;
        this.newGameType = newType;
    }

    public GameType getCurrentGameType() { return currentGameType; }
    public GameType getNewGameType() { return newGameType; }
}
