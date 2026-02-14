package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;

public record MonsterRoomMob(Weight weight) {
    public static final Codec<MonsterRoomMob> WEIGHT_CODEC = Weight.CODEC
            .xmap(MonsterRoomMob::new, MonsterRoomMob::weight);
    public static final Codec<MonsterRoomMob> CODEC = RecordCodecBuilder.create(in -> in.group(
            Weight.CODEC.fieldOf("weight").forGetter(MonsterRoomMob::weight)
    ).apply(in, MonsterRoomMob::new));
}
