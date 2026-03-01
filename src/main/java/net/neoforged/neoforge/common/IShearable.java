package net.neoforged.neoforge.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stub: Interface for shearable entities.
 */
public interface IShearable {

    boolean isShearable(@Nullable Player player, ItemStack item, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos);

    List<ItemStack> onSheared(@Nullable Player player, ItemStack item, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos);
}
