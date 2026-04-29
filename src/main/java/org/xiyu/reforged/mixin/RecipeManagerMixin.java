package org.xiyu.reforged.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes recipe deserialization error-tolerant.
 *
 * <p>Vanilla/Forge only catches {@code IllegalArgumentException | JsonParseException}
 * in the recipe parsing loop. NeoForge mod codecs may throw other runtime exceptions
 * (e.g. IndexOutOfBoundsException from Create's SequencedRecipe) that escape the
 * catch block and crash the game.</p>
 *
 * <p>This mixin wraps the {@code Codec.parse()} call to convert any exception into
 * a {@code DataResult.error()}, which is then handled gracefully by the existing
 * error-handling code path.</p>
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    private static final Logger REFORGED_LOGGER = LogManager.getLogger("ReForged");
    private static final Set<String> REFORGED_UNKNOWN_CONDITIONS = ConcurrentHashMap.newKeySet();
    private static final Set<String> REFORGED_NORMALIZER_WARNINGS = ConcurrentHashMap.newKeySet();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
        ),
        remap = false
    )
    private DataResult reforged$safeRecipeParse(Codec codec, DynamicOps ops, Object input) {
        try {
            return codec.parse(ops, reforged$normalizeNeoForgeRecipeJson(input));
        } catch (Exception e) {
            REFORGED_LOGGER.error("[ReForged] Recipe codec threw exception during parse: {}", e.getMessage());
            return DataResult.error(() -> "[ReForged] Recipe codec exception: " + e.getMessage());
        }
    }

    @Redirect(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/ForgeHooks;readAndTestCondition(Lnet/minecraft/resources/RegistryOps;Lcom/google/gson/JsonObject;)Z"
        ),
        remap = false
    )
    private boolean reforged$readAndTestNeoForgeConditions(RegistryOps<JsonElement> ops, JsonObject recipeJson) {
        ConditionResult conditionResult = reforged$testNeoForgeConditions(recipeJson);
        if (!conditionResult.matches()) {
            return false;
        }

        JsonObject forgeJson = recipeJson;
        if (conditionResult.consumedForgeConditionsKey()) {
            forgeJson = recipeJson.deepCopy();
            forgeJson.remove("conditions");
        }
        return ForgeHooks.readAndTestCondition(ops, forgeJson);
    }

    private static Object reforged$normalizeNeoForgeRecipeJson(Object input) {
        if (input instanceof JsonElement element) {
            return reforged$normalizeNeoForgeJson(element);
        }
        return input;
    }

    private static JsonElement reforged$normalizeNeoForgeJson(JsonElement element) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return element;
        }
        if (element.isJsonArray()) {
            JsonArray normalized = new JsonArray();
            for (JsonElement child : element.getAsJsonArray()) {
                normalized.add(reforged$normalizeNeoForgeJson(child));
            }
            return normalized;
        }

        JsonObject normalized = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            normalized.add(entry.getKey(), reforged$normalizeNeoForgeJson(entry.getValue()));
        }

        String type = reforged$getString(normalized, "type");
        if (type == null) {
            return normalized;
        }

        return switch (type) {
            case "neoforge:single" -> reforged$asCreateFluidStack(normalized);
            case "neoforge:components" -> reforged$normalizeComponentIngredient(normalized);
            case "neoforge:tag" -> reforged$asCreateFluidTag(normalized);
            case "neoforge:block_tag" -> reforged$expandBlockTagIngredient(normalized);
            case "neoforge:compound" -> reforged$flattenCompoundIngredient(normalized);
            default -> normalized;
        };
    }

    private static JsonObject reforged$asCreateFluidStack(JsonObject json) {
        json.addProperty("type", "fluid_stack");
        return json;
    }

    private static JsonObject reforged$asCreateComponentFluidStack(JsonObject json) {
        json.addProperty("type", "fluid_stack");
        if (!json.has("fluid") && json.has("fluids")) {
            json.add("fluid", json.remove("fluids"));
        }
        return json;
    }

    private static JsonElement reforged$normalizeComponentIngredient(JsonObject json) {
        if (json.has("items") || json.has("item") || json.has("tag")) {
            return reforged$asVanillaItemIngredient(json);
        }
        return reforged$asCreateComponentFluidStack(json);
    }

    private static JsonElement reforged$asVanillaItemIngredient(JsonObject json) {
        JsonElement items = json.has("items") ? json.remove("items") : json.get("item");
        if (items == null) {
            return json;
        }

        if (items.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement item : items.getAsJsonArray()) {
                JsonObject entry = new JsonObject();
                entry.add("item", item);
                result.add(entry);
            }
            return result;
        }

        JsonObject result = new JsonObject();
        result.add("item", items);
        return result;
    }

    private static JsonObject reforged$asCreateFluidTag(JsonObject json) {
        json.addProperty("type", "fluid_tag");
        if (!json.has("fluid_tag") && json.has("tag")) {
            json.add("fluid_tag", json.remove("tag"));
        }
        return json;
    }

    private static JsonElement reforged$flattenCompoundIngredient(JsonObject json) {
        JsonElement ingredientsElement = json.get("ingredients");
        if (ingredientsElement == null || !ingredientsElement.isJsonArray()) {
            reforged$warnNormalizerOnce("compound-missing-ingredients",
                    "NeoForge compound ingredient is missing an ingredients array; leaving it unchanged");
            return json;
        }

        JsonArray ingredients = ingredientsElement.getAsJsonArray();
        if (ingredients.size() == 0) {
            return json;
        }
        if (ingredients.size() == 1) {
            return ingredients.get(0);
        }
        return ingredients;
    }

    private static JsonElement reforged$expandBlockTagIngredient(JsonObject json) {
        String tagName = reforged$getString(json, "tag");
        ResourceLocation tagId = tagName == null ? null : ResourceLocation.tryParse(tagName);
        if (tagId == null) {
            reforged$warnNormalizerOnce("block-tag-invalid",
                    "NeoForge block tag ingredient has an invalid tag id; leaving it unchanged");
            return json;
        }

        JsonArray items = new JsonArray();
        TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, tagId);
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag)) {
            Item item = holder.value().asItem();
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId == null) {
                continue;
            }

            JsonObject itemIngredient = new JsonObject();
            itemIngredient.addProperty("item", itemId.toString());
            items.add(itemIngredient);
        }

        if (items.size() == 0) {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("tag", tagId.toString());
            return fallback;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        return items;
    }

    private static ConditionResult reforged$testNeoForgeConditions(JsonObject recipeJson) {
        JsonElement conditions = recipeJson.get("neoforge:conditions");
        if (conditions != null && !conditions.isJsonNull()) {
            return new ConditionResult(reforged$testConditionList(conditions), false);
        }

        conditions = recipeJson.get("conditions");
        if (conditions == null || conditions.isJsonNull() || !reforged$containsNeoForgeCondition(conditions)) {
            return new ConditionResult(true, false);
        }
        return new ConditionResult(reforged$testConditionList(conditions), true);
    }

    private static boolean reforged$containsNeoForgeCondition(JsonElement conditions) {
        if (conditions == null || conditions.isJsonNull()) {
            return false;
        }
        if (conditions.isJsonObject()) {
            String type = reforged$getString(conditions.getAsJsonObject(), "type");
            return type != null && type.startsWith("neoforge:");
        }
        if (conditions.isJsonArray()) {
            for (JsonElement condition : conditions.getAsJsonArray()) {
                if (reforged$containsNeoForgeCondition(condition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean reforged$testConditionList(JsonElement conditions) {
        if (conditions == null || conditions.isJsonNull()) {
            return true;
        }
        if (conditions.isJsonObject()) {
            return reforged$testNeoForgeCondition(conditions.getAsJsonObject());
        }
        if (!conditions.isJsonArray()) {
            reforged$warnConditionOnce("malformed-list",
                    "Malformed neoforge:conditions value; skipping guarded recipe");
            return false;
        }

        for (JsonElement condition : conditions.getAsJsonArray()) {
            if (!condition.isJsonObject() || !reforged$testNeoForgeCondition(condition.getAsJsonObject())) {
                return false;
            }
        }
        return true;
    }

    private static boolean reforged$testNeoForgeCondition(JsonObject condition) {
        String type = reforged$getString(condition, "type");
        if (type == null) {
            reforged$warnConditionOnce("missing-type", "NeoForge condition is missing a type; skipping guarded recipe");
            return false;
        }

        return switch (type) {
            case "neoforge:true", "forge:true" -> true;
            case "neoforge:false", "forge:false" -> false;
            case "neoforge:mod_loaded", "forge:mod_loaded" -> reforged$isModLoaded(condition);
            case "neoforge:not", "forge:not" -> !reforged$testNestedCondition(condition, "value", "condition");
            case "neoforge:and", "forge:and" -> reforged$testNestedConditionList(condition);
            case "neoforge:or", "forge:or" -> reforged$testAnyNestedCondition(condition);
            case "neoforge:item_exists", "forge:item_exists" -> reforged$itemExists(condition);
            case "neoforge:tag_empty", "forge:tag_empty" -> reforged$isItemTagEmpty(condition);
            default -> {
                reforged$warnConditionOnce(type, "Unknown NeoForge condition type '" + type
                        + "'; skipping guarded recipe to avoid loading incompatible data");
                yield false;
            }
        };
    }

    private static boolean reforged$isModLoaded(JsonObject condition) {
        String modId = reforged$getString(condition, "modid");
        return modId != null && ModList.get().isLoaded(modId);
    }

    private static boolean reforged$itemExists(JsonObject condition) {
        String itemName = reforged$getString(condition, "item");
        ResourceLocation itemId = itemName == null ? null : ResourceLocation.tryParse(itemName);
        return itemId != null && BuiltInRegistries.ITEM.containsKey(itemId);
    }

    private static boolean reforged$isItemTagEmpty(JsonObject condition) {
        String tagName = reforged$getString(condition, "tag");
        ResourceLocation tagId = tagName == null ? null : ResourceLocation.tryParse(tagName);
        if (tagId == null) {
            return true;
        }
        return !BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, tagId)).iterator().hasNext();
    }

    private static boolean reforged$testNestedCondition(JsonObject condition, String primaryKey, String fallbackKey) {
        JsonElement nested = condition.has(primaryKey) ? condition.get(primaryKey) : condition.get(fallbackKey);
        if (nested == null || !nested.isJsonObject()) {
            reforged$warnConditionOnce("malformed-nested",
                    "Malformed nested NeoForge condition; skipping guarded recipe");
            return false;
        }
        return reforged$testNeoForgeCondition(nested.getAsJsonObject());
    }

    private static boolean reforged$testNestedConditionList(JsonObject condition) {
        return reforged$testConditionList(reforged$getConditionArray(condition));
    }

    private static boolean reforged$testAnyNestedCondition(JsonObject condition) {
        JsonElement children = reforged$getConditionArray(condition);
        if (children == null || !children.isJsonArray()) {
            reforged$warnConditionOnce("malformed-or",
                    "Malformed NeoForge OR condition; skipping guarded recipe");
            return false;
        }
        for (JsonElement child : children.getAsJsonArray()) {
            if (child.isJsonObject() && reforged$testNeoForgeCondition(child.getAsJsonObject())) {
                return true;
            }
        }
        return false;
    }

    private static JsonElement reforged$getConditionArray(JsonObject condition) {
        if (condition.has("values")) {
            return condition.get("values");
        }
        if (condition.has("conditions")) {
            return condition.get("conditions");
        }
        return condition.get("children");
    }

    private static String reforged$getString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? element.getAsString()
                : null;
    }

    private static void reforged$warnConditionOnce(String key, String message) {
        if (REFORGED_UNKNOWN_CONDITIONS.add(key)) {
            REFORGED_LOGGER.warn("[ReForged] {}", message);
        }
    }

    private static void reforged$warnNormalizerOnce(String key, String message) {
        if (REFORGED_NORMALIZER_WARNINGS.add(key)) {
            REFORGED_LOGGER.warn("[ReForged] {}", message);
        }
    }

    private record ConditionResult(boolean matches, boolean consumedForgeConditionsKey) {}
}
