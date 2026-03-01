package net.minecraft.data.recipes;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;

public class SpecialRecipeBuilder {
    private final Function<CraftingBookCategory, Recipe<?>> factory;

    public SpecialRecipeBuilder(Function<CraftingBookCategory, Recipe<?>> p_312302_) {
        this.factory = p_312302_;
    }

    public static SpecialRecipeBuilder special(Function<CraftingBookCategory, Recipe<?>> p_310896_) {
        return new SpecialRecipeBuilder(p_310896_);
    }

    public void save(RecipeOutput p_301326_, String p_299862_) {
        this.save(p_301326_, ResourceLocation.parse(p_299862_));
    }

    public void save(RecipeOutput p_301231_, ResourceLocation p_297560_) {
        p_301231_.accept(p_297560_, this.factory.apply(CraftingBookCategory.MISC), null);
    }
}