package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.Ingredient;

public record ArmorMaterial(
    Map<ArmorItem.Type, Integer> defense,
    int enchantmentValue,
    Holder<SoundEvent> equipSound,
    Supplier<Ingredient> repairIngredient,
    List<ArmorMaterial.Layer> layers,
    float toughness,
    float knockbackResistance
) {
    public static final Codec<Holder<ArmorMaterial>> CODEC = BuiltInRegistries.ARMOR_MATERIAL.holderByNameCodec();

    public int getDefense(ArmorItem.Type p_328867_) {
        return this.defense.getOrDefault(p_328867_, 0);
    }

    public static final class Layer {
        private final ResourceLocation assetName;
        private final String suffix;
        private final boolean dyeable;
        private final ResourceLocation innerTexture;
        private final ResourceLocation outerTexture;

        public Layer(ResourceLocation p_328120_, String p_329928_, boolean p_329101_) {
            this.assetName = p_328120_;
            this.suffix = p_329928_;
            this.dyeable = p_329101_;
            this.innerTexture = this.resolveTexture(true);
            this.outerTexture = this.resolveTexture(false);
        }

        public Layer(ResourceLocation p_330953_) {
            this(p_330953_, "", false);
        }

        private ResourceLocation resolveTexture(boolean p_329608_) {
            return this.assetName
                .withPath(p_330916_ -> "textures/models/armor/" + this.assetName.getPath() + "_layer_" + (p_329608_ ? 2 : 1) + this.suffix + ".png");
        }

        public ResourceLocation texture(boolean p_329648_) {
            return p_329648_ ? this.innerTexture : this.outerTexture;
        }

        public boolean dyeable() {
            return this.dyeable;
        }

        public String getSuffix() {
            return this.suffix;
        }
    }
}
