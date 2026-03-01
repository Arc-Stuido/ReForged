package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe, net.minecraftforge.common.crafting.IShapedRecipe<CraftingInput> {
    static int MAX_WIDTH = 3;
    static int MAX_HEIGHT = 3;
    /**
     * Expand the max width and height allowed in the deserializer.
     * This should be called by modders who add custom crafting tables that are larger than the vanilla 3x3.
     * @param width your max recipe width
     * @param height your max recipe height
     */
    public static void setCraftingSize(int width, int height) {
        if (MAX_WIDTH < width) MAX_WIDTH = width;
        if (MAX_HEIGHT < height) MAX_HEIGHT = height;
    }
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipe(String p_250221_, CraftingBookCategory p_250716_, ShapedRecipePattern p_312200_, ItemStack p_248581_, boolean p_310619_) {
        this.group = p_250221_;
        this.category = p_250716_;
        this.pattern = p_312200_;
        this.result = p_248581_;
        this.showNotification = p_310619_;
    }

    public ShapedRecipe(String p_272759_, CraftingBookCategory p_273506_, ShapedRecipePattern p_310709_, ItemStack p_272852_) {
        this(p_272759_, p_273506_, p_310709_, p_272852_, true);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider p_332111_) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int p_44161_, int p_44162_) {
        return p_44161_ >= this.pattern.width() && p_44162_ >= this.pattern.height();
    }

    public boolean matches(CraftingInput p_345171_, Level p_44177_) {
        return this.pattern.matches(p_345171_);
    }

    public ItemStack assemble(CraftingInput p_345083_, HolderLookup.Provider p_333236_) {
        return this.getResultItem(p_333236_).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public int getRecipeWidth() {
        return getWidth();
    }

    @Override
    public int getRecipeHeight() {
        return getHeight();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = this.getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().filter(p_151277_ -> !p_151277_.isEmpty()).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_327208_ -> p_327208_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(p_309251_ -> p_309251_.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_309253_ -> p_309253_.category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(p_309254_ -> p_309254_.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_309252_ -> p_309252_.result),
                        Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(p_309255_ -> p_309255_.showNotification)
                    )
                    .apply(p_327208_, ShapedRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(
            ShapedRecipe.Serializer::toNetwork, ShapedRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedRecipe fromNetwork(RegistryFriendlyByteBuf p_335571_) {
            String s = p_335571_.readUtf();
            CraftingBookCategory craftingbookcategory = p_335571_.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.STREAM_CODEC.decode(p_335571_);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(p_335571_);
            boolean flag = p_335571_.readBoolean();
            return new ShapedRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_336365_, ShapedRecipe p_330934_) {
            p_336365_.writeUtf(p_330934_.group);
            p_336365_.writeEnum(p_330934_.category);
            ShapedRecipePattern.STREAM_CODEC.encode(p_336365_, p_330934_.pattern);
            ItemStack.STREAM_CODEC.encode(p_336365_, p_330934_.result);
            p_336365_.writeBoolean(p_330934_.showNotification);
        }
    }
}
