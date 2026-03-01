package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeOutput {
    default void accept(ResourceLocation p_310578_, Recipe<?> p_312265_, @Nullable AdvancementHolder p_310407_) {
        if (p_310407_ == null) {
            accept(p_310578_, p_312265_, null, null);
        } else {
            var ops = registry().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var json = Advancement.CODEC.encodeStart(ops, p_310407_.value()).getOrThrow(IllegalStateException::new);
            accept(p_310578_, p_312265_, p_310407_.id(), json);
        }
    }

    void accept(ResourceLocation id, Recipe<?> recipe, @Nullable ResourceLocation advancementId, @Nullable com.google.gson.JsonElement advancement);

    net.minecraft.core.HolderLookup.Provider registry();

    Advancement.Builder advancement();
}
