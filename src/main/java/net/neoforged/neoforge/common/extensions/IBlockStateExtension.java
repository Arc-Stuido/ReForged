package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Stub extension interface for BlockState.
 */
public interface IBlockStateExtension {

    default BlockState self() {
        return (BlockState) this;
    }

    default float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return self().getBlock().getFriction();
    }

    default boolean hasDynamicLightEmission() {
        return false;
    }

    default int getLightEmission(BlockGetter level, BlockPos pos) {
        return self().getLightEmission();
    }

    default boolean isLadder(LevelReader level, BlockPos pos, LivingEntity entity) {
        return false;
    }

    default boolean canHarvestBlock(BlockGetter level, BlockPos pos, Player player) {
        return true;
    }

    default boolean ignitedByLava(BlockGetter level, BlockPos pos, Direction face) {
        return false;
    }

    default boolean isBurning(BlockGetter level, BlockPos pos) {
        return false;
    }
}
