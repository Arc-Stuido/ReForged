package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;

/**
 * Stub for NeoForge's FluidIngredientType — type token for fluid ingredient dispatch codecs.
 */
public interface FluidIngredientType<T extends FluidIngredient> {
    MapCodec<T> codec();
}
