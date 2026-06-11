package net.neoforged.neoforge.common.brewing;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for custom brewing recipes.
 *
 * <p>Extends Forge's {@code IBrewingRecipe} (identical shape) so shim-typed
 * recipes can be passed straight into Forge's {@code PotionBrewing.Builder.add}.</p>
 */
public interface IBrewingRecipe extends net.minecraftforge.common.brewing.IBrewingRecipe {
    /**
     * Returns true if the given item stack is a valid input for this recipe.
     */
    boolean isInput(ItemStack input);

    /**
     * Returns true if the given item stack is a valid ingredient (reagent) for this recipe.
     */
    boolean isIngredient(ItemStack ingredient);

    /**
     * Returns the output of this recipe given the input and ingredient.
     * Returns {@link ItemStack#EMPTY} if the combination is not valid.
     */
    ItemStack getOutput(ItemStack input, ItemStack ingredient);
}
