package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Stub for NeoForge's FluidIngredient — represents a fluid ingredient in recipes.
 * Provides enough codec infrastructure that Create's CreateCodecs can initialize.
 */
public class FluidIngredient implements Predicate<FluidStack> {

    public static final FluidIngredient EMPTY = new FluidIngredient();

    /**
     * Stub MapCodec — always decodes to EMPTY, encodes nothing.
     * This is sufficient for Create's RecordCodecBuilder usage in CreateCodecs.
     */
    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY =
            RecordCodecBuilder.mapCodec(instance -> instance.point(EMPTY));

    public static final Codec<FluidIngredient> CODEC = MAP_CODEC_NONEMPTY.codec();
    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = CODEC;
    public static final Codec<List<FluidIngredient>> LIST_CODEC = CODEC.listOf();

    /**
     * StreamCodec for network serialization. Stub: writes 0 stacks, always decodes to EMPTY.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                FluidStack.STREAM_CODEC.decode(buf); // skip
            }
            return EMPTY;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            buf.writeVarInt(0);
        }
    };

    public FluidIngredient() {
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public FluidStack[] getStacks() {
        return new FluidStack[0];
    }

    public boolean hasNoFluids() {
        return true;
    }

    public FluidIngredientType<?> getType() {
        return null;
    }

    public List<FluidStack> fluids() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static <T extends FluidIngredient> MapCodec<T> codec(FluidIngredientType<T> type) {
        return (MapCodec<T>) MAP_CODEC_NONEMPTY;
    }
}
