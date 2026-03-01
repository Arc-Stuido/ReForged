package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractCookingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<?> type;
    protected final CookingBookCategory category;
    protected final String group;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int cookingTime;

    public AbstractCookingRecipe(
        RecipeType<?> p_250197_, String p_249518_, CookingBookCategory p_250891_, Ingredient p_251354_, ItemStack p_252185_, float p_252165_, int p_250256_
    ) {
        this.type = p_250197_;
        this.category = p_250891_;
        this.group = p_249518_;
        this.ingredient = p_251354_;
        this.result = p_252185_;
        this.experience = p_252165_;
        this.cookingTime = p_250256_;
    }

    public boolean matches(SingleRecipeInput p_343646_, Level p_345123_) {
        return this.ingredient.test(p_343646_.item());
    }

    public ItemStack assemble(SingleRecipeInput p_344932_, HolderLookup.Provider p_335957_) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43743_, int p_43744_) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(this.ingredient);
        return nonnulllist;
    }

    public float getExperience() {
        return this.experience;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_328377_) {
        return this.result;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public CookingBookCategory category() {
        return this.category;
    }

    public interface Factory<T extends AbstractCookingRecipe> {
        T create(String p_310191_, CookingBookCategory p_311031_, Ingredient p_313122_, ItemStack p_312156_, float p_312177_, int p_311374_);
    }
}