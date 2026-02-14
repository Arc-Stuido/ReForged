package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record RaidHeroGift(ResourceKey<LootTable> lootTable) {
    public static final Codec<RaidHeroGift> LOOT_TABLE_CODEC = ResourceKey.codec(Registries.LOOT_TABLE)
            .xmap(RaidHeroGift::new, RaidHeroGift::lootTable);
    public static final Codec<RaidHeroGift> CODEC = LOOT_TABLE_CODEC;
}
