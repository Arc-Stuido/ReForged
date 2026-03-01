package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
    private final RecipeCache.Entry[] entries;
    private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

    public RecipeCache(int p_309405_) {
        this.entries = new RecipeCache.Entry[p_309405_];
    }

    public Optional<RecipeHolder<CraftingRecipe>> get(Level p_311354_, CraftingInput p_342819_) {
        if (p_342819_.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(p_311354_);

            for (int i = 0; i < this.entries.length; i++) {
                RecipeCache.Entry recipecache$entry = this.entries[i];
                if (recipecache$entry != null && recipecache$entry.matches(p_342819_)) {
                    this.moveEntryToFront(i);
                    return Optional.ofNullable(recipecache$entry.value());
                }
            }

            return this.compute(p_342819_, p_311354_);
        }
    }

    private void validateRecipeManager(Level p_310788_) {
        RecipeManager recipemanager = p_310788_.getRecipeManager();
        if (recipemanager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference<>(recipemanager);
            Arrays.fill(this.entries, null);
        }
    }

    private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput p_345296_, Level p_309968_) {
        Optional<RecipeHolder<CraftingRecipe>> optional = p_309968_.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, p_345296_, p_309968_);
        this.insert(p_345296_, optional.orElse(null));
        return optional;
    }

    private void moveEntryToFront(int p_309395_) {
        if (p_309395_ > 0) {
            RecipeCache.Entry recipecache$entry = this.entries[p_309395_];
            System.arraycopy(this.entries, 0, this.entries, 1, p_309395_);
            this.entries[0] = recipecache$entry;
        }
    }

    private void insert(CraftingInput p_342978_, @Nullable RecipeHolder<CraftingRecipe> p_330177_) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_342978_.size(), ItemStack.EMPTY);

        for (int i = 0; i < p_342978_.size(); i++) {
            nonnulllist.set(i, p_342978_.getItem(i).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.Entry(nonnulllist, p_342978_.width(), p_342978_.height(), p_330177_);
    }

    static record Entry(NonNullList<ItemStack> key, int width, int height, @Nullable RecipeHolder<CraftingRecipe> value) {
        public boolean matches(CraftingInput p_344906_) {
            if (this.width == p_344906_.width() && this.height == p_344906_.height()) {
                for (int i = 0; i < this.key.size(); i++) {
                    if (!ItemStack.isSameItemSameComponents(this.key.get(i), p_344906_.getItem(i))) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }
}