package net.minecraft.world.item.armortrim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ArmorTrim implements TooltipProvider {
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
        p_327186_ -> p_327186_.group(
                    TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material),
                    TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(p_327187_ -> p_327187_.showInTooltip)
                )
                .apply(p_327186_, ArmorTrim::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(
        TrimMaterial.STREAM_CODEC,
        ArmorTrim::material,
        TrimPattern.STREAM_CODEC,
        ArmorTrim::pattern,
        ByteBufCodecs.BOOL,
        p_327185_ -> p_327185_.showInTooltip,
        ArmorTrim::new
    );
    private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.upgrade")))
        .withStyle(ChatFormatting.GRAY);
    private final Holder<TrimMaterial> material;
    private final Holder<TrimPattern> pattern;
    private final boolean showInTooltip;
    private final Function<Holder<ArmorMaterial>, ResourceLocation> innerTexture;
    private final Function<Holder<ArmorMaterial>, ResourceLocation> outerTexture;

    private ArmorTrim(
        Holder<TrimMaterial> p_330892_,
        Holder<TrimPattern> p_328752_,
        boolean p_335455_,
        Function<Holder<ArmorMaterial>, ResourceLocation> p_332214_,
        Function<Holder<ArmorMaterial>, ResourceLocation> p_329213_
    ) {
        this.material = p_330892_;
        this.pattern = p_328752_;
        this.showInTooltip = p_335455_;
        this.innerTexture = p_332214_;
        this.outerTexture = p_329213_;
    }

    public ArmorTrim(Holder<TrimMaterial> p_336165_, Holder<TrimPattern> p_333838_, boolean p_331209_) {
        this.material = p_336165_;
        this.pattern = p_333838_;
        this.innerTexture = Util.memoize(p_327184_ -> {
            ResourceLocation resourcelocation = p_333838_.value().assetId();
            String s = getColorPaletteSuffix(p_336165_, p_327184_);
            return resourcelocation.withPath(p_266737_ -> "trims/models/armor/" + p_266737_ + "_leggings_" + s);
        });
        this.outerTexture = Util.memoize(p_327190_ -> {
            ResourceLocation resourcelocation = p_333838_.value().assetId();
            String s = getColorPaletteSuffix(p_336165_, p_327190_);
            return resourcelocation.withPath(p_266864_ -> "trims/models/armor/" + p_266864_ + "_" + s);
        });
        this.showInTooltip = p_331209_;
    }

    public ArmorTrim(Holder<TrimMaterial> p_267249_, Holder<TrimPattern> p_267212_) {
        this(p_267249_, p_267212_, true);
    }

    private static String getColorPaletteSuffix(Holder<TrimMaterial> p_329303_, Holder<ArmorMaterial> p_330126_) {
        Map<Holder<ArmorMaterial>, String> map = p_329303_.value().overrideArmorMaterials();
        String s = map.get(p_330126_);
        return s != null ? s : p_329303_.value().assetName();
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> p_266942_, Holder<TrimMaterial> p_267247_) {
        return p_266942_.equals(this.pattern) && p_267247_.equals(this.material);
    }

    public Holder<TrimPattern> pattern() {
        return this.pattern;
    }

    public Holder<TrimMaterial> material() {
        return this.material;
    }

    public ResourceLocation innerTexture(Holder<ArmorMaterial> p_330423_) {
        return this.innerTexture.apply(p_330423_);
    }

    public ResourceLocation outerTexture(Holder<ArmorMaterial> p_334845_) {
        return this.outerTexture.apply(p_334845_);
    }

    @Override
    public boolean equals(Object p_267123_) {
        return !(p_267123_ instanceof ArmorTrim armortrim)
            ? false
            : this.showInTooltip == armortrim.showInTooltip && this.pattern.equals(armortrim.pattern) && this.material.equals(armortrim.material);
    }

    @Override
    public int hashCode() {
        int i = this.material.hashCode();
        i = 31 * i + this.pattern.hashCode();
        return 31 * i + (this.showInTooltip ? 1 : 0);
    }

    @Override
    public void addToTooltip(Item.TooltipContext p_334801_, Consumer<Component> p_334147_, TooltipFlag p_329892_) {
        if (this.showInTooltip) {
            p_334147_.accept(UPGRADE_TITLE);
            p_334147_.accept(CommonComponents.space().append(this.pattern.value().copyWithStyle(this.material)));
            p_334147_.accept(CommonComponents.space().append(this.material.value().description()));
        }
    }

    public ArmorTrim withTooltip(boolean p_332005_) {
        return new ArmorTrim(this.material, this.pattern, p_332005_, this.innerTexture, this.outerTexture);
    }
}