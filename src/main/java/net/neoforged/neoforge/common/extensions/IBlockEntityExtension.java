package net.neoforged.neoforge.common.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stub extension interface for BlockEntity.
 */
public interface IBlockEntityExtension {

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    }

    default void handleUpdateTag(CompoundTag tag) {
    }

    default CompoundTag getPersistentData() {
        return new CompoundTag();
    }

    default void onChunkUnloaded() {
    }

    default void onLoad() {
    }

    default void requestModelDataUpdate() {
    }

    default boolean hasCustomOutlineRendering(Player player) {
        return false;
    }

    default void invalidateCapabilities() {
    }
}
