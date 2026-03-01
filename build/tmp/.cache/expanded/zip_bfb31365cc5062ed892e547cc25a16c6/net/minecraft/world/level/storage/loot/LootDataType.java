package net.minecraft.world.level.storage.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.DataResult.Error;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public record LootDataType<T>(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, createLootTableValidator());

    public void runValidation(ValidationContext p_279366_, ResourceKey<T> p_329223_, T p_279124_) {
        this.validator.run(p_279366_, p_329223_, p_279124_);
    }

    public <V> Optional<T> deserialize(ResourceLocation p_279253_, DynamicOps<V> p_335939_, V p_327740_) {
        DataResult<T> dataresult = this.codec.parse(p_335939_, p_327740_);
        dataresult.error()
            .ifPresent(p_341974_ -> LOGGER.error("Couldn't parse element {}/{} - {}", this.registryKey.location(), p_279253_, p_341974_.message()));
        var ret = dataresult.result();
        if (ret.orElse(null) instanceof LootTable table) {
            table.setLootTableId(p_279253_);
            ret = Optional.ofNullable((T)net.minecraftforge.event.ForgeEventFactory.onLoadLootTable(p_279253_, table));
        }
        return ret;
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, TABLE);
    }

    private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
        return (p_327548_, p_327549_, p_327550_) -> p_327550_.validate(
                p_327548_.enterElement("{" + p_327549_.registry() + "/" + p_327549_.location() + "}", p_327549_)
            );
    }

    private static LootDataType.Validator<LootTable> createLootTableValidator() {
        return (p_327551_, p_327552_, p_327553_) -> p_327553_.validate(
                p_327551_.setParams(p_327553_.getParamSet()).enterElement("{" + p_327552_.registry() + "/" + p_327552_.location() + "}", p_327552_)
            );
    }

    @FunctionalInterface
    public interface Validator<T> {
        void run(ValidationContext p_279419_, ResourceKey<T> p_334619_, T p_279326_);
    }
}
