package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe extends CustomRecipe {
    public DecoratedPotRecipe(CraftingBookCategory p_273056_) {
        super(p_273056_);
    }

    public boolean matches(CraftingInput p_342524_, Level p_272812_) {
        if (!this.canCraftInDimensions(p_342524_.width(), p_342524_.height())) {
            return false;
        } else {
            for (int i = 0; i < p_342524_.size(); i++) {
                ItemStack itemstack = p_342524_.getItem(i);
                switch (i) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                        if (!itemstack.is(ItemTags.DECORATED_POT_INGREDIENTS)) {
                            return false;
                        }
                        break;
                    case 2:
                    case 4:
                    case 6:
                    default:
                        if (!itemstack.is(Items.AIR)) {
                            return false;
                        }
                }
            }

            return true;
        }
    }

    public ItemStack assemble(CraftingInput p_344747_, HolderLookup.Provider p_328495_) {
        PotDecorations potdecorations = new PotDecorations(
            p_344747_.getItem(1).getItem(), p_344747_.getItem(3).getItem(), p_344747_.getItem(5).getItem(), p_344747_.getItem(7).getItem()
        );
        return DecoratedPotBlockEntity.createDecoratedPotItem(potdecorations);
    }

    @Override
    public boolean canCraftInDimensions(int p_273734_, int p_273516_) {
        return p_273734_ == 3 && p_273516_ == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}