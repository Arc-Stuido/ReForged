package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/**
 * Stub: NeoForge's DataMapValueMerger functional interface.
 */
@FunctionalInterface
public interface DataMapValueMerger<R, T> {
    T merge(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> first, T firstValue,
            Either<TagKey<R>, ResourceKey<R>> second, T secondValue);

    static <T, R> DataMapValueMerger<R, T> defaultMerger() {
        return (registry, first, firstValue, second, secondValue) -> secondValue;
    }

    static <T, R> DataMapValueMerger<R, List<T>> listMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final List<T> list = new ArrayList<>(firstValue);
            list.addAll(secondValue);
            return list;
        };
    }

    static <T, R> DataMapValueMerger<R, Set<T>> setMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final Set<T> set = new HashSet<>(firstValue);
            set.addAll(secondValue);
            return set;
        };
    }

    static <K, V, R> DataMapValueMerger<R, Map<K, V>> mapMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final Map<K, V> map = new HashMap<>(firstValue);
            map.putAll(secondValue);
            return map;
        };
    }
}
