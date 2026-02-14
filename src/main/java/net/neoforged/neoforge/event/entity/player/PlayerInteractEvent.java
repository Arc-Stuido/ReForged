package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.player.PlayerInteractEvent}. */
public class PlayerInteractEvent {
    private final net.minecraftforge.event.entity.player.PlayerInteractEvent delegate;

    public PlayerInteractEvent() { this.delegate = null; }

    public PlayerInteractEvent(net.minecraftforge.event.entity.player.PlayerInteractEvent delegate) {
        this.delegate = delegate;
    }

    public Player getEntity() { return delegate.getEntity(); }
    public InteractionHand getHand() { return delegate.getHand(); }
    public ItemStack getItemStack() { return delegate.getItemStack(); }
    public BlockPos getPos() { return delegate.getPos(); }
    public Direction getFace() { return delegate.getFace(); }

    public static class EntityInteract extends PlayerInteractEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract forgeInteract;

        public EntityInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract delegate) {
            super(delegate);
            this.forgeInteract = delegate;
        }

        public Entity getTarget() { return forgeInteract.getTarget(); }
    }

    public static class EntityInteractSpecific extends PlayerInteractEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific forgeEvent;

        public EntityInteractSpecific(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public Entity getTarget() { return forgeEvent.getTarget(); }
    }

    public static class RightClickBlock extends PlayerInteractEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock forgeEvent;

        public RightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public BlockHitResult getHitVec() { return forgeEvent.getHitVec(); }
    }

    public static class RightClickItem extends PlayerInteractEvent {
        public RightClickItem(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem delegate) {
            super(delegate);
        }
    }

    public static class LeftClickEmpty extends PlayerInteractEvent {
        public LeftClickEmpty(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty delegate) {
            super(delegate);
        }
    }

    public static class LeftClickBlock extends PlayerInteractEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock forgeEvent;

        public LeftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public enum Action { ABORT, DENY, ALLOW }
    }
}
