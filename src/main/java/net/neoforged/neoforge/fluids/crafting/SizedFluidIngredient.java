package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stub for NeoForge's SizedFluidIngredient — a FluidIngredient with an amount.
 */
public class SizedFluidIngredient {

    public static final MapCodec<SizedFluidIngredient> FLAT_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(SizedFluidIngredient::amount)
            ).apply(instance, SizedFluidIngredient::new));

    public static final Codec<SizedFluidIngredient> NESTED_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(SizedFluidIngredient::amount)
            ).apply(instance, SizedFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.STREAM_CODEC,
            SizedFluidIngredient::ingredient,
            ByteBufCodecs.VAR_INT,
            SizedFluidIngredient::amount,
            SizedFluidIngredient::new);

    private final FluidIngredient ingredient;
    private final int amount;

    public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public FluidIngredient ingredient() {
        return ingredient;
    }

    public int amount() {
        return amount;
    }

    public static SizedFluidIngredient of(FluidIngredient ingredient, int amount) {
        return new SizedFluidIngredient(ingredient, amount);
    }
}
