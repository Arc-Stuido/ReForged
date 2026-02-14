package net.neoforged.neoforge.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;

/** Wrapper around Forge's {@link net.minecraftforge.client.event.MovementInputUpdateEvent}. */
public class MovementInputUpdateEvent {
    private final net.minecraftforge.client.event.MovementInputUpdateEvent delegate;

    public MovementInputUpdateEvent(net.minecraftforge.client.event.MovementInputUpdateEvent delegate) {
        this.delegate = delegate;
    }

    public Player getEntity() { return delegate.getEntity(); }
    public Input getInput() { return delegate.getInput(); }
}
