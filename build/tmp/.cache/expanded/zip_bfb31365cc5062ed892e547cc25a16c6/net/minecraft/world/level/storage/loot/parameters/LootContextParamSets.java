package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public class LootContextParamSets {
    private static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
    public static final Codec<LootContextParamSet> CODEC = ResourceLocation.CODEC
        .comapFlatMap(
            p_297179_ -> Optional.ofNullable(REGISTRY.get(p_297179_))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No parameter set exists with id: '" + p_297179_ + "'")),
            REGISTRY.inverse()::get
        );
    public static final LootContextParamSet EMPTY = register("empty", p_81454_ -> {
    });
    public static final LootContextParamSet CHEST = register(
        "chest", p_327636_ -> p_327636_.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.ATTACKING_ENTITY) //Forge: Chest Minecarts can have killers.
    );
    public static final LootContextParamSet COMMAND = register(
        "command", p_327634_ -> p_327634_.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet SELECTOR = register(
        "selector", p_81442_ -> p_81442_.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet FISHING = register(
        "fishing", p_81446_ -> p_81446_.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.ATTACKING_ENTITY).optional(LootContextParams.THIS_ENTITY) //Forge: Allow fisher, and bobber
    );
    public static final LootContextParamSet ENTITY = register(
        "entity",
        p_342016_ -> p_342016_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.DAMAGE_SOURCE)
                .optional(LootContextParams.ATTACKING_ENTITY)
                .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
                .optional(LootContextParams.LAST_DAMAGE_PLAYER)
    );
    public static final LootContextParamSet EQUIPMENT = register(
        "equipment", p_327637_ -> p_327637_.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet ARCHAEOLOGY = register(
        "archaeology", p_81452_ -> p_81452_.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet GIFT = register(
        "gift", p_81448_ -> p_81448_.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet PIGLIN_BARTER = register("barter", p_81440_ -> p_81440_.required(LootContextParams.THIS_ENTITY));
    public static final LootContextParamSet VAULT = register(
        "vault", p_81450_ -> p_81450_.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet ADVANCEMENT_REWARD = register(
        "advancement_reward", p_81438_ -> p_81438_.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
    );
    public static final LootContextParamSet ADVANCEMENT_ENTITY = register(
        "advancement_entity", p_81436_ -> p_81436_.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
    );
    public static final LootContextParamSet ADVANCEMENT_LOCATION = register(
        "advancement_location",
        p_286218_ -> p_286218_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.TOOL)
                .required(LootContextParams.BLOCK_STATE)
    );
    public static final LootContextParamSet BLOCK_USE = register(
        "block_use", p_327633_ -> p_327633_.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE)
    );
    public static final LootContextParamSet ALL_PARAMS = register(
        "generic",
        p_342015_ -> p_342015_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.LAST_DAMAGE_PLAYER)
                .required(LootContextParams.DAMAGE_SOURCE)
                .required(LootContextParams.ATTACKING_ENTITY)
                .required(LootContextParams.DIRECT_ATTACKING_ENTITY)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.BLOCK_STATE)
                .required(LootContextParams.BLOCK_ENTITY)
                .required(LootContextParams.TOOL)
                .required(LootContextParams.EXPLOSION_RADIUS)
    );
    public static final LootContextParamSet BLOCK = register(
        "block",
        p_81425_ -> p_81425_.required(LootContextParams.BLOCK_STATE)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.TOOL)
                .optional(LootContextParams.THIS_ENTITY)
                .optional(LootContextParams.BLOCK_ENTITY)
                .optional(LootContextParams.EXPLOSION_RADIUS)
    );
    public static final LootContextParamSet SHEARING = register(
        "shearing", p_272589_ -> p_272589_.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
    );
    public static final LootContextParamSet ENCHANTED_DAMAGE = register(
        "enchanted_damage",
        p_342018_ -> p_342018_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.ENCHANTMENT_LEVEL)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.DAMAGE_SOURCE)
                .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
                .optional(LootContextParams.ATTACKING_ENTITY)
    );
    public static final LootContextParamSet ENCHANTED_ITEM = register(
        "enchanted_item", p_342017_ -> p_342017_.required(LootContextParams.TOOL).required(LootContextParams.ENCHANTMENT_LEVEL)
    );
    public static final LootContextParamSet ENCHANTED_LOCATION = register(
        "enchanted_location",
        p_342019_ -> p_342019_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.ENCHANTMENT_LEVEL)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.ENCHANTMENT_ACTIVE)
    );
    public static final LootContextParamSet ENCHANTED_ENTITY = register(
        "enchanted_entity",
        p_342014_ -> p_342014_.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN)
    );
    public static final LootContextParamSet HIT_BLOCK = register(
        "hit_block",
        p_342020_ -> p_342020_.required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.ENCHANTMENT_LEVEL)
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.BLOCK_STATE)
    );

    private static LootContextParamSet register(String p_81429_, Consumer<LootContextParamSet.Builder> p_81430_) {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        p_81430_.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace(p_81429_);
        LootContextParamSet lootcontextparamset1 = REGISTRY.put(resourcelocation, lootcontextparamset);
        if (lootcontextparamset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
        } else {
            return lootcontextparamset;
        }
    }
}
