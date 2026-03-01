package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.entity.LivingEntity;

/**
 * Stub extension interface for LivingEntity.
 */
public interface ILivingEntityExtension extends IEntityExtension {

    default LivingEntity self() {
        return (LivingEntity) this;
    }

    default boolean canSwimInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }

    default void jumpInFluid(net.neoforged.neoforge.fluids.FluidType type) {
    }

    default void sinkInFluid(net.neoforged.neoforge.fluids.FluidType type) {
    }

    default boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }
}
