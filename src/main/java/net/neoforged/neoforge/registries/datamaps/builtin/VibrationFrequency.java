package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VibrationFrequency(int frequency) {
    public static final Codec<VibrationFrequency> FREQUENCY_CODEC = Codec.INT
            .xmap(VibrationFrequency::new, VibrationFrequency::frequency);
    public static final Codec<VibrationFrequency> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.INT.fieldOf("frequency").forGetter(VibrationFrequency::frequency)
    ).apply(in, VibrationFrequency::new));
}
