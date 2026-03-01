package net.minecraft.core;

import net.minecraft.resources.ResourceKey;

public interface WritableRegistry<T> extends Registry<T> {
    Holder.Reference<T> register(ResourceKey<T> p_256320_, T p_255978_, RegistrationInfo p_329112_);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}