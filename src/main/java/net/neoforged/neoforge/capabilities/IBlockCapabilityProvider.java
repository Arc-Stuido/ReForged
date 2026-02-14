package net.neoforged.neoforge.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's IBlockCapabilityProvider — for block-level capability providers.
 */
@FunctionalInterface
public interface IBlockCapabilityProvider<T, C extends @Nullable Object> {
    @Nullable
    T getCapability(Level level, BlockPos pos, BlockState state,
                    @Nullable BlockEntity blockEntity, C context);
}
