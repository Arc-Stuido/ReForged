package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Compostable(float chance, boolean canVillagerCompost) {
    public Compostable(float chance) {
        this(chance, false);
    }

    public static final Codec<Compostable> CHANCE_CODEC = Codec.FLOAT
            .xmap(Compostable::new, Compostable::chance);
    public static final Codec<Compostable> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.FLOAT.fieldOf("chance").forGetter(Compostable::chance),
            Codec.BOOL.optionalFieldOf("can_villager_compost", false).forGetter(Compostable::canVillagerCompost)
    ).apply(in, Compostable::new));
}
