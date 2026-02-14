package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.player.PlayerEvent}. */
public class PlayerEvent {
    private final net.minecraftforge.event.entity.player.PlayerEvent delegate;

    public PlayerEvent() { this.delegate = null; }

    public PlayerEvent(net.minecraftforge.event.entity.player.PlayerEvent delegate) {
        this.delegate = delegate;
    }

    public Player getEntity() { return delegate.getEntity(); }

    public static class StartTracking extends PlayerEvent {
        private final net.minecraftforge.event.entity.player.PlayerEvent.StartTracking forgeEvent;

        public StartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public Entity getTarget() { return forgeEvent.getTarget(); }
    }

    public static class Clone extends PlayerEvent {
        private final net.minecraftforge.event.entity.player.PlayerEvent.Clone forgeEvent;

        public Clone(net.minecraftforge.event.entity.player.PlayerEvent.Clone delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public Player getOriginal() { return forgeEvent.getOriginal(); }
        public boolean isWasDeath() { return forgeEvent.isWasDeath(); }
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent delegate) {
            super(delegate);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent delegate) {
            super(delegate);
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {
        private final net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent forgeEvent;

        public PlayerRespawnEvent(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public boolean isEndConquered() { return forgeEvent.isEndConquered(); }
    }

    public static class ItemCraftedEvent extends PlayerEvent {
        public ItemCraftedEvent(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent delegate) {
            super(delegate);
        }
    }
}
