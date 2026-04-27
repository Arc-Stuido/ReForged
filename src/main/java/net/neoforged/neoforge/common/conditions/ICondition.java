package net.neoforged.neoforge.common.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge's ICondition interface for conditional loading of data-driven content.
 */
public interface ICondition {
    /**
     * Codec for a single condition, dispatching by codec() on each implementation.
     */
    Codec<ICondition> CODEC = Codec.of(ICondition::encodeCondition, ICondition::decodeCondition);

    /**
     * Codec for a list of conditions.
     */
    Codec<List<ICondition>> LIST_CODEC = CODEC.listOf();

    boolean test(IContext context);

    default MapCodec<? extends ICondition> codec() {
        return MapCodec.unit(this);
    }

    private static <T> DataResult<T> encodeCondition(ICondition condition, DynamicOps<T> ops, T prefix) {
        var builder = ops.mapBuilder();
        if (condition instanceof TrueCondition) {
            builder.add("type", ops.createString("neoforge:true"));
            return builder.build(prefix);
        }
        if (condition instanceof FalseCondition) {
            builder.add("type", ops.createString("neoforge:false"));
            return builder.build(prefix);
        }
        if (condition instanceof ModLoadedCondition modLoaded) {
            builder.add("type", ops.createString("neoforge:mod_loaded"));
            builder.add("modid", ops.createString(modLoaded.modId()));
            return builder.build(prefix);
        }
        if (condition instanceof NotCondition not) {
            builder.add("type", ops.createString("neoforge:not"));
            builder.add("value", CODEC.encodeStart(ops, not.value()));
            return builder.build(prefix);
        }
        if (condition instanceof AndCondition and) {
            builder.add("type", ops.createString("neoforge:and"));
            builder.add("values", LIST_CODEC.encodeStart(ops, and.conditions()));
            return builder.build(prefix);
        }
        if (condition instanceof OrCondition or) {
            builder.add("type", ops.createString("neoforge:or"));
            builder.add("values", LIST_CODEC.encodeStart(ops, or.conditions()));
            return builder.build(prefix);
        }
        if (condition instanceof ItemExistsCondition itemExists) {
            builder.add("type", ops.createString("neoforge:item_exists"));
            builder.add("item", ops.createString(itemExists.item().toString()));
            return builder.build(prefix);
        }
        if (condition instanceof TagEmptyCondition tagEmpty) {
            builder.add("type", ops.createString("neoforge:tag_empty"));
            builder.add("tag", ops.createString(tagEmpty.tag().toString()));
            return builder.build(prefix);
        }
        return DataResult.error(() -> "Unknown NeoForge condition implementation: " + condition.getClass().getName());
    }

    private static <T> DataResult<Pair<ICondition, T>> decodeCondition(DynamicOps<T> ops, T input) {
        return decodeConditionValue(ops, input).map(condition -> Pair.of(condition, input));
    }

    private static <T> DataResult<ICondition> decodeConditionValue(DynamicOps<T> ops, T input) {
        return ops.getMap(input).flatMap(map -> {
            String type = getString(ops, map, "type");
            if (type == null) {
                return DataResult.success(FalseCondition.INSTANCE);
            }

            return switch (type) {
                case "neoforge:true", "forge:true" -> DataResult.success(TrueCondition.INSTANCE);
                case "neoforge:false", "forge:false" -> DataResult.success(FalseCondition.INSTANCE);
                case "neoforge:mod_loaded", "forge:mod_loaded" -> {
                    String modId = getString(ops, map, "modid");
                    yield modId == null
                            ? DataResult.success(FalseCondition.INSTANCE)
                            : DataResult.success(new ModLoadedCondition(modId));
                }
                case "neoforge:not", "forge:not" -> decodeNestedCondition(ops, map, "value", "condition")
                        .map(NotCondition::new);
                case "neoforge:and", "forge:and" -> decodeConditionList(ops, map)
                        .map(AndCondition::new);
                case "neoforge:or", "forge:or" -> decodeConditionList(ops, map)
                        .map(OrCondition::new);
                case "neoforge:item_exists", "forge:item_exists" -> {
                    ResourceLocation item = parseLocation(getString(ops, map, "item"));
                    yield item == null
                            ? DataResult.success(FalseCondition.INSTANCE)
                            : DataResult.success(new ItemExistsCondition(item));
                }
                case "neoforge:tag_empty", "forge:tag_empty" -> {
                    ResourceLocation tag = parseLocation(getString(ops, map, "tag"));
                    yield tag == null
                            ? DataResult.success(TrueCondition.INSTANCE)
                            : DataResult.success(new TagEmptyCondition(tag));
                }
                default -> DataResult.success(FalseCondition.INSTANCE);
            };
        });
    }

    private static <T> DataResult<ICondition> decodeNestedCondition(DynamicOps<T> ops, MapLike<T> map,
                                                                     String primaryKey, String fallbackKey) {
        T nested = map.get(primaryKey);
        if (nested == null) {
            nested = map.get(fallbackKey);
        }
        if (nested == null) {
            return DataResult.success(FalseCondition.INSTANCE);
        }
        return CODEC.decode(ops, nested).map(Pair::getFirst);
    }

    private static <T> DataResult<List<ICondition>> decodeConditionList(DynamicOps<T> ops, MapLike<T> map) {
        T children = map.get("values");
        if (children == null) {
            children = map.get("conditions");
        }
        if (children == null) {
            children = map.get("children");
        }
        if (children == null) {
            return DataResult.success(List.of());
        }
        return LIST_CODEC.decode(ops, children).map(Pair::getFirst);
    }

    private static <T> String getString(DynamicOps<T> ops, MapLike<T> map, String key) {
        T value = map.get(key);
        return value == null ? null : ops.getStringValue(value).result().orElse(null);
    }

    private static ResourceLocation parseLocation(String value) {
        return value == null ? null : ResourceLocation.tryParse(value);
    }

    interface IContext {
        IContext EMPTY = new IContext() {};

        /**
         * Return all tags for the given registry.
         */
        default <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
            return Collections.emptyMap();
        }
    }
}
