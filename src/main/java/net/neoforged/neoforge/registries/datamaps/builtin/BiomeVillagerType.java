package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerType;

public record BiomeVillagerType(VillagerType type) {
    public static final Codec<BiomeVillagerType> TYPE_CODEC = BuiltInRegistries.VILLAGER_TYPE.byNameCodec()
            .xmap(BiomeVillagerType::new, BiomeVillagerType::type);
    public static final Codec<BiomeVillagerType> CODEC = TYPE_CODEC;
}
