package net.neoforged.neoforge.common;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge MutableDataComponentHolder — extends DataComponentHolder with mutation methods.
 */
public interface MutableDataComponentHolder extends DataComponentHolder {
    @Nullable
    <T> T set(DataComponentType<? super T> componentType, @Nullable T value);

    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<? super T>> componentType, @Nullable T value) {
        return set(componentType.get(), value);
    }

    @Nullable
    default <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value), updateContext));
    }

    @Nullable
    default <T, U> T update(Supplier<? extends DataComponentType<T>> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return update(componentType.get(), value, updateContext, updater);
    }

    @Nullable
    default <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value)));
    }

    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> componentType, T value, UnaryOperator<T> updater) {
        return update(componentType.get(), value, updater);
    }

    @Nullable
    <T> T remove(DataComponentType<? extends T> componentType);

    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return remove(componentType.get());
    }

    @SuppressWarnings("unchecked")
    default void copyFrom(DataComponentHolder src, DataComponentType<?>... componentTypes) {
        for (var componentType : componentTypes) {
            copyFromInternal(componentType, src);
        }
    }

    @SuppressWarnings("unchecked")
    default void copyFrom(DataComponentHolder src, Supplier<? extends DataComponentType<?>>... componentTypes) {
        for (var componentType : componentTypes) {
            copyFromInternal(componentType.get(), src);
        }
    }

    void applyComponents(DataComponentPatch patch);

    void applyComponents(DataComponentMap components);

    @SuppressWarnings("unchecked")
    private <T> void copyFromInternal(DataComponentType<T> componentType, DataComponentHolder src) {
        set(componentType, src.get(componentType));
    }
}
