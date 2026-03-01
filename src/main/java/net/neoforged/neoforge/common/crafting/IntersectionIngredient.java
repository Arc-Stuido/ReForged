package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Stub: Ingredient that requires a stack to match ALL of its sub-ingredients.
 */
public class IntersectionIngredient extends Ingredient {
    private final List<Ingredient> children;

    @SuppressWarnings("unchecked")
    private IntersectionIngredient(List<Ingredient> children) {
        super(children.stream().flatMap(i -> Arrays.stream(i.values).map(v -> (Ingredient.Value) v)));
        this.children = children;
    }

    public static Ingredient of(Ingredient... ingredients) {
        return new IntersectionIngredient(List.of(ingredients));
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (!child.test(stack)) return false;
        }
        return true;
    }
}
