package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FurnaceFuel(int burnTime) {
    public static final Codec<FurnaceFuel> BURN_TIME_CODEC = Codec.INT
            .xmap(FurnaceFuel::new, FurnaceFuel::burnTime);
    public static final Codec<FurnaceFuel> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.INT.fieldOf("burn_time").forGetter(FurnaceFuel::burnTime)
    ).apply(in, FurnaceFuel::new));
}
