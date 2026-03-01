package net.neoforged.neoforge.fluids;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Stub: Represents a fluid stored in a cauldron.
 */
public record CauldronFluidContent(Block block, Fluid fluid, int maxLevel) {
}
