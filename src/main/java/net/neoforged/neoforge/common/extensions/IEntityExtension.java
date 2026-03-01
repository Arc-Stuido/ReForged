package net.neoforged.neoforge.common.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Stub extension interface for Entity.
 */
public interface IEntityExtension {

    default Entity self() {
        return (Entity) this;
    }

    @Nullable
    default Collection<ItemEntity> captureDrops() {
        return null;
    }

    default Collection<ItemEntity> captureDrops(@Nullable Collection<ItemEntity> drops) {
        return java.util.Collections.emptyList();
    }

    default CompoundTag getPersistentData() {
        return new CompoundTag();
    }

    default boolean shouldRiderSit() {
        return true;
    }

    @Nullable
    default ItemStack getPickedResult(HitResult target) {
        return null;
    }

    default boolean canRiderInteract() {
        return false;
    }

    default boolean canUpdate() {
        return true;
    }

    default void canUpdate(boolean value) {
    }

    default CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        return new CompoundTag();
    }

    default void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt) {
    }
}
