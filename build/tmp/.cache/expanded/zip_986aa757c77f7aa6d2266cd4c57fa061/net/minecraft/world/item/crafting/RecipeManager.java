package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HolderLookup.Provider registries;
    public Multimap<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.of();
    public Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
    private boolean hasErrors;
    private final net.minecraftforge.common.crafting.conditions.ICondition.IContext context; //Forge: add context

    /** @deprecated Forge: use {@linkplain RecipeManager#RecipeManager(net.minecraftforge.common.crafting.conditions.ICondition.IContext) constructor with context}. */
    public RecipeManager(HolderLookup.Provider p_330459_) {
        this(p_330459_, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
    }

    public RecipeManager(HolderLookup.Provider p_330459_, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
        super(GSON, Registries.elementsDirPath(Registries.RECIPE));
        this.registries = p_330459_;
        this.context = context;
    }

    protected void apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_) {
        this.hasErrors = false;
        Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder1 = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.registries.createSerializationContext(JsonOps.INSTANCE)
            .withContext(net.minecraftforge.common.crafting.conditions.ICondition.IContext.KEY, this.context);

        for (Entry<ResourceLocation, JsonElement> entry : p_44037_.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                if (entry.getValue().isJsonObject() && !net.minecraftforge.common.ForgeHooks.readAndTestCondition(registryops, entry.getValue().getAsJsonObject())) {
                    LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", resourcelocation);
                    continue;
                }
                Recipe<?> recipe = Recipe.CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeHolder<?> recipeholder = new RecipeHolder<>(resourcelocation, recipe);
                builder.put(recipe.getType(), recipeholder);
                builder1.put(resourcelocation, recipeholder);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading recipe {}", resourcelocation, jsonparseexception);
            }
        }

        this.byType = builder.build();
        this.byName = builder1.build();
        LOGGER.info("Loaded {} recipes", this.byType.size());
    }

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> p_44016_, I p_344358_, Level p_44018_) {
        return this.getRecipeFor(p_44016_, p_344358_, p_44018_, (RecipeHolder<T>)null);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
        RecipeType<T> p_220249_, I p_344518_, Level p_220251_, @Nullable ResourceLocation p_220252_
    ) {
        RecipeHolder<T> recipeholder = p_220252_ != null ? this.byKeyTyped(p_220249_, p_220252_) : null;
        return this.getRecipeFor(p_220249_, p_344518_, p_220251_, recipeholder);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
        RecipeType<T> p_343647_, I p_342793_, Level p_344483_, @Nullable RecipeHolder<T> p_345187_
    ) {
        if (p_342793_.isEmpty()) {
            return Optional.empty();
        } else {
            return p_345187_ != null && p_345187_.value().matches(p_342793_, p_344483_)
                ? Optional.of(p_345187_)
                : this.byType(p_343647_).stream().filter(p_341585_ -> p_341585_.value().matches(p_342793_, p_344483_)).findFirst();
        }
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> p_44014_) {
        return List.copyOf(this.byType(p_44014_));
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> p_44057_, I p_345126_, Level p_44059_) {
        return this.byType(p_44057_)
            .stream()
            .filter(p_341582_ -> p_341582_.value().matches(p_345126_, p_44059_))
            .sorted(Comparator.comparing(p_327196_ -> p_327196_.value().getResultItem(p_44059_.registryAccess()).getDescriptionId()))
            .collect(Collectors.toList());
    }

    private <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> p_44055_) {
        return (Collection)this.byType.get(p_44055_);
    }

    public <I extends RecipeInput, T extends Recipe<I>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> p_44070_, I p_345480_, Level p_44072_) {
        Optional<RecipeHolder<T>> optional = this.getRecipeFor(p_44070_, p_345480_, p_44072_);
        if (optional.isPresent()) {
            return optional.get().value().getRemainingItems(p_345480_);
        } else {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_345480_.size(), ItemStack.EMPTY);

            for (int i = 0; i < nonnulllist.size(); i++) {
                nonnulllist.set(i, p_345480_.getItem(i));
            }

            return nonnulllist;
        }
    }

    public Optional<RecipeHolder<?>> byKey(ResourceLocation p_44044_) {
        return Optional.ofNullable(this.byName.get(p_44044_));
    }

    @Nullable
    private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> p_332930_, ResourceLocation p_335282_) {
        RecipeHolder<?> recipeholder = this.byName.get(p_335282_);
        return (RecipeHolder<T>)(recipeholder != null && recipeholder.value().getType().equals(p_332930_) ? recipeholder : null);
    }

    public Collection<RecipeHolder<?>> getOrderedRecipes() {
        return this.byType.values();
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.byName.values();
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.byName.keySet().stream();
    }

    @VisibleForTesting
    protected static RecipeHolder<?> fromJson(ResourceLocation p_44046_, JsonObject p_44047_, HolderLookup.Provider p_328308_) {
        Recipe<?> recipe = Recipe.CODEC.parse(p_328308_.createSerializationContext(JsonOps.INSTANCE), p_44047_).getOrThrow(JsonParseException::new);
        return new RecipeHolder<>(p_44046_, recipe);
    }

    public void replaceRecipes(Iterable<RecipeHolder<?>> p_44025_) {
        this.hasErrors = false;
        Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder1 = ImmutableMap.builder();

        for (RecipeHolder<?> recipeholder : p_44025_) {
            RecipeType<?> recipetype = recipeholder.value().getType();
            builder.put(recipetype, recipeholder);
            builder1.put(recipeholder.id(), recipeholder);
        }

        this.byType = builder.build();
        this.byName = builder1.build();
    }

    public static <I extends RecipeInput, T extends Recipe<I>> RecipeManager.CachedCheck<I, T> createCheck(final RecipeType<T> p_220268_) {
        return new RecipeManager.CachedCheck<I, T>() {
            @Nullable
            private ResourceLocation lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(I p_343525_, Level p_220279_) {
                RecipeManager recipemanager = p_220279_.getRecipeManager();
                Optional<RecipeHolder<T>> optional = recipemanager.getRecipeFor(p_220268_, p_343525_, p_220279_, this.lastRecipe);
                if (optional.isPresent()) {
                    RecipeHolder<T> recipeholder = optional.get();
                    this.lastRecipe = recipeholder.id();
                    return Optional.of(recipeholder);
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
        Optional<RecipeHolder<T>> getRecipeFor(I p_343520_, Level p_220281_);
    }
}
