package net.neoforged.neoforge.registries.datamaps.builtin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Stub: NeoForge's built-in data map types.
 */
public class NeoForgeDataMaps {

    public static final DataMapType<net.minecraft.world.item.Item, Compostable> COMPOSTABLES =
            DataMapType.builder(id("compostables"), Registries.ITEM, Compostable.CODEC)
                    .synced(Compostable.CHANCE_CODEC, false).build();

    public static final DataMapType<net.minecraft.world.item.Item, FurnaceFuel> FURNACE_FUELS =
            DataMapType.builder(id("furnace_fuels"), Registries.ITEM, FurnaceFuel.CODEC)
                    .synced(FurnaceFuel.BURN_TIME_CODEC, false).build();

    public static final DataMapType<net.minecraft.world.entity.EntityType<?>, MonsterRoomMob> MONSTER_ROOM_MOBS =
            DataMapType.builder(id("monster_room_mobs"), Registries.ENTITY_TYPE, MonsterRoomMob.CODEC)
                    .synced(MonsterRoomMob.WEIGHT_CODEC, false).build();

    public static final DataMapType<net.minecraft.world.level.block.Block, Oxidizable> OXIDIZABLES =
            DataMapType.builder(id("oxidizables"), Registries.BLOCK, Oxidizable.CODEC)
                    .synced(Oxidizable.CODEC, false).build();

    public static final DataMapType<net.minecraft.world.entity.EntityType<?>, ParrotImitation> PARROT_IMITATIONS =
            DataMapType.builder(id("parrot_imitations"), Registries.ENTITY_TYPE, ParrotImitation.CODEC)
                    .synced(ParrotImitation.CODEC, false).build();

    public static final DataMapType<net.minecraft.world.entity.npc.VillagerProfession, RaidHeroGift> RAID_HERO_GIFTS =
            DataMapType.builder(id("raid_hero_gifts"), Registries.VILLAGER_PROFESSION, RaidHeroGift.CODEC)
                    .synced(RaidHeroGift.CODEC, false).build();

    public static final DataMapType<net.minecraft.world.level.gameevent.GameEvent, VibrationFrequency> VIBRATION_FREQUENCIES =
            DataMapType.builder(id("vibration_frequencies"), Registries.GAME_EVENT, VibrationFrequency.CODEC)
                    .synced(VibrationFrequency.FREQUENCY_CODEC, false).build();

    public static final DataMapType<net.minecraft.world.level.biome.Biome, BiomeVillagerType> VILLAGER_TYPES =
            DataMapType.builder(id("villager_types"), Registries.BIOME, BiomeVillagerType.CODEC).build();

    public static final DataMapType<net.minecraft.world.level.block.Block, Waxable> WAXABLES =
            DataMapType.builder(id("waxables"), Registries.BLOCK, Waxable.CODEC)
                    .synced(Waxable.CODEC, false).build();

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", name);
    }
}
