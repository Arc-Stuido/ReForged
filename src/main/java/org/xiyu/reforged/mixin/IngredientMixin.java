package org.xiyu.reforged.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds NeoForge-specific methods to {@link Ingredient} that NeoForge mods expect.
 * <p>In NeoForge, {@code hasNoItems()} is added by {@code IIngredientExtension}.</p>
 */
@Mixin(Ingredient.class)
public abstract class IngredientMixin {

    @Shadow
    public abstract ItemStack[] getItems();

    /**
     * NeoForge's {@code IIngredientExtension.hasNoItems()} — returns true if this ingredient
     * matches no items at all. Used by Twilight Forest's NoTemplateSmithingRecipe.
     */
    public boolean hasNoItems() {
        return getItems().length == 0;
    }
}
