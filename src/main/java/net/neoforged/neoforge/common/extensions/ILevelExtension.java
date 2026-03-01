package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Stub extension interface for Level.
 */
public interface ILevelExtension {

    String TRANSLATION_PREFIX = "dimension";

    default Level self() {
        return (Level) this;
    }

    default double getMaxEntityRadius() {
        return 2.0D;
    }

    default double increaseMaxEntityRadius(double value) {
        return getMaxEntityRadius();
    }

    /**
     * Query a block capability at the given position (5-arg form used by NeoForge mods like Jade).
     * Delegates to {@link BlockCapability#getCapability} which bridges to Forge's capability system.
     */
    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos,
                                    @Nullable BlockState state, @Nullable BlockEntity blockEntity,
                                    @Nullable C context) {
        return cap.getCapability(self(), pos, state, blockEntity, context);
    }

    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos, @Nullable C context) {
        return cap.getCapability(self(), pos, null, null, context);
    }

    @Nullable
    default <T> T getCapability(BlockCapability<T, Void> cap, BlockPos pos) {
        return getCapability(cap, pos, null, null, null);
    }

    default void invalidateCapabilities(BlockPos pos) {
    }

    default void invalidateCapabilities(ChunkPos pos) {
    }

    default String getDescriptionKey() {
        return TRANSLATION_PREFIX + "." + self().dimension().location().toLanguageKey();
    }

    default Component getDescription() {
        return Component.translatable(getDescriptionKey());
    }
}
