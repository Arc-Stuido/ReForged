package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Stub: Fired when a player opens/closes a container.
 */
public class PlayerContainerEvent extends PlayerEvent {
    private final AbstractContainerMenu container;

    public PlayerContainerEvent(Player player, AbstractContainerMenu container) {
        super();
        this.container = container;
    }

    public AbstractContainerMenu getContainer() { return container; }

    public static class Open extends PlayerContainerEvent {
        public Open(Player player, AbstractContainerMenu container) {
            super(player, container);
        }
    }

    public static class Close extends PlayerContainerEvent {
        public Close(Player player, AbstractContainerMenu container) {
            super(player, container);
        }
    }
}
