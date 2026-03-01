package net.minecraft.core.component;

import javax.annotation.Nullable;

public interface DataComponentHolder {
    DataComponentMap getComponents();

    @Nullable
    default <T> T get(DataComponentType<? extends T> p_331483_) {
        return this.getComponents().get(p_331483_);
    }

    default <T> T getOrDefault(DataComponentType<? extends T> p_328483_, T p_333219_) {
        return this.getComponents().getOrDefault(p_328483_, p_333219_);
    }

    default boolean has(DataComponentType<?> p_333597_) {
        return this.getComponents().has(p_333597_);
    }
}