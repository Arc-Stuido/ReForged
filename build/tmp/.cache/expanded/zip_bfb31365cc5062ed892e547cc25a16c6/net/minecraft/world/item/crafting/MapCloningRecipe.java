package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MapCloningRecipe extends CustomRecipe {
    public MapCloningRecipe(CraftingBookCategory p_251985_) {
        super(p_251985_);
    }

    public boolean matches(CraftingInput p_342926_, Level p_43981_) {
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < p_342926_.size(); j++) {
            ItemStack itemstack1 = p_342926_.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(Items.FILLED_MAP)) {
                    if (!itemstack.isEmpty()) {
                        return false;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(Items.MAP)) {
                        return false;
                    }

                    i++;
                }
            }
        }

        return !itemstack.isEmpty() && i > 0;
    }

    public ItemStack assemble(CraftingInput p_344433_, HolderLookup.Provider p_334317_) {
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < p_344433_.size(); j++) {
            ItemStack itemstack1 = p_344433_.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(Items.FILLED_MAP)) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(Items.MAP)) {
                        return ItemStack.EMPTY;
                    }

                    i++;
                }
            }
        }

        return !itemstack.isEmpty() && i >= 1 ? itemstack.copyWithCount(i + 1) : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43970_, int p_43971_) {
        return p_43970_ >= 3 && p_43971_ >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}