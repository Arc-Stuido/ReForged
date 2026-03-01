package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: Fired when a player uses an item on a block.
 */
public class UseItemOnBlockEvent extends Event {
    private final Level level;
    @Nullable
    private final net.minecraft.world.entity.player.Player player;
    private final InteractionHand hand;
    private final ItemStack heldItem;
    private final BlockPos pos;
    @Nullable
    private final Direction face;
    private final UseOnContext context;
    private final UsePhase usePhase;

    public UseItemOnBlockEvent(UseOnContext context, UsePhase usePhase) {
        this.level = context.getLevel();
        this.player = context.getPlayer();
        this.heldItem = context.getItemInHand();
        this.hand = context.getHand();
        this.pos = context.getClickedPos();
        this.face = context.getClickedFace();
        this.context = context;
        this.usePhase = usePhase;
    }

    @Nullable public net.minecraft.world.entity.player.Player getPlayer() { return player; }
    public InteractionHand getHand() { return hand; }
    public ItemStack getItemStack() { return heldItem; }
    public BlockPos getPos() { return pos; }
    @Nullable public Direction getFace() { return face; }
    public Level getLevel() { return level; }
    public UseOnContext getUseOnContext() { return context; }
    public UsePhase getUsePhase() { return usePhase; }

    public enum UsePhase {
        ITEM_BEFORE_BLOCK,
        BLOCK,
        ITEM_AFTER_BLOCK
    }
}
