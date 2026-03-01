package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(Ingredient p_266750_, Ingredient p_266787_, Ingredient p_267292_, ItemStack p_267031_) {
        this.template = p_266750_;
        this.base = p_266787_;
        this.addition = p_267292_;
        this.result = p_267031_;
    }

    public boolean matches(SmithingRecipeInput p_343371_, Level p_266781_) {
        return this.template.test(p_343371_.template()) && this.base.test(p_343371_.base()) && this.addition.test(p_343371_.addition());
    }

    public ItemStack assemble(SmithingRecipeInput p_343590_, HolderLookup.Provider p_331030_) {
        ItemStack itemstack = p_343590_.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_330801_) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack p_267113_) {
        return this.template.test(p_267113_);
    }

    @Override
    public boolean isBaseIngredient(ItemStack p_267276_) {
        return this.base.test(p_267276_);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack p_267260_) {
        return this.addition.test(p_267260_);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_327220_ -> p_327220_.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(p_297231_ -> p_297231_.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(p_298250_ -> p_298250_.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(p_299654_ -> p_299654_.addition),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_297480_ -> p_297480_.result)
                    )
                    .apply(p_327220_, SmithingTransformRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.of(
            SmithingTransformRecipe.Serializer::toNetwork, SmithingTransformRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingTransformRecipe fromNetwork(RegistryFriendlyByteBuf p_333917_) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(p_333917_);
            return new SmithingTransformRecipe(ingredient, ingredient1, ingredient2, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_329920_, SmithingTransformRecipe p_266927_) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.addition);
            ItemStack.STREAM_CODEC.encode(p_329920_, p_266927_.result);
        }
    }
}
