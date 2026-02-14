package net.neoforged.neoforge.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapEntry;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

/**
 * Stub: NeoForge's DataMapProvider — data-generation provider for the data map system.
 * Provides builder() methods and Builder/AdvancedBuilder inner classes.
 */
public abstract class DataMapProvider {

    protected DataMapProvider() {
    }

    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(DataMapType<R, T> type) {
        return new Builder<>(type);
    }

    @SuppressWarnings("unchecked")
    public <T, R, VR extends DataMapValueRemover<R, T>> AdvancedBuilder<T, R, VR> builder(AdvancedDataMapType<R, T, VR> type) {
        return new AdvancedBuilder<>(type);
    }

    public static class Builder<T, R> {
        protected final DataMapType<R, T> type;
        protected boolean replace;
        protected final Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values = new HashMap<>();
        protected final List<Object> removals = new ArrayList<>();

        Builder(DataMapType<R, T> type) {
            this.type = type;
        }

        public Builder<T, R> add(ResourceKey<R> key, T value, boolean replace, ICondition... conditions) {
            values.put(Either.right(key), Optional.of(
                    conditions.length > 0
                            ? new WithConditions<>(List.of(conditions), new DataMapEntry<>(value, replace))
                            : new WithConditions<>(new DataMapEntry<>(value, replace))));
            return this;
        }

        public Builder<T, R> add(ResourceLocation id, T value, boolean replace, ICondition... conditions) {
            return add(ResourceKey.create(type.registryKey(), id), value, replace, conditions);
        }

        public Builder<T, R> add(Holder<R> object, T value, boolean replace, ICondition... conditions) {
            object.unwrapKey().ifPresent(key -> add(key, value, replace, conditions));
            return this;
        }

        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace, ICondition... conditions) {
            values.put(Either.left(tag), Optional.of(
                    conditions.length > 0
                            ? new WithConditions<>(List.of(conditions), new DataMapEntry<>(value, replace))
                            : new WithConditions<>(new DataMapEntry<>(value, replace))));
            return this;
        }

        public Builder<T, R> remove(ResourceLocation id) {
            removals.add(id);
            return this;
        }

        public Builder<T, R> remove(TagKey<R> tag) {
            removals.add(tag);
            return this;
        }

        public Builder<T, R> remove(Holder<R> value) {
            value.unwrapKey().ifPresent(key -> removals.add(key.location()));
            return this;
        }

        public Builder<T, R> replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public WithConditions<DataMapFile<T, R>> build() {
            return new WithConditions<>(new DataMapFile<>(replace, values, removals));
        }
    }

    public static class AdvancedBuilder<T, R, VR extends DataMapValueRemover<R, T>> extends Builder<T, R> {
        AdvancedBuilder(AdvancedDataMapType<R, T, VR> type) {
            super(type);
        }

        public AdvancedBuilder<T, R, VR> remove(TagKey<R> tag, VR remover) {
            removals.add(tag);
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(Holder<R> value, VR remover) {
            value.unwrapKey().ifPresent(key -> removals.add(key.location()));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(ResourceLocation id, VR remover) {
            removals.add(id);
            return this;
        }
    }
}
