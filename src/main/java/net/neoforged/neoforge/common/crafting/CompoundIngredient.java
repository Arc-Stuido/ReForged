package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * NeoForge CompoundIngredient bridge — delegates to Forge's CompoundIngredient.
 */
public class CompoundIngredient {

    /**
     * Creates a compound ingredient from the given list of ingredients.
     * Delegates to Forge's equivalent implementation.
     */
    public static Ingredient of(Ingredient... children) {
        if (children.length == 0) return Ingredient.EMPTY;
        if (children.length == 1) return children[0];
        return net.minecraftforge.common.crafting.ingredients.CompoundIngredient.of(children);
    }
}
