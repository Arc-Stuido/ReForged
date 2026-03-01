package net.neoforged.neoforge.common;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Stub: Interface for handling minecart collisions.
 */
public interface IMinecartCollisionHandler {

    void onEntityCollision(AbstractMinecart cart, net.minecraft.world.entity.Entity other);

    @Nullable
    AABB getCollisionBox(AbstractMinecart cart, net.minecraft.world.entity.Entity other);

    AABB getMinecartCollisionBox(AbstractMinecart cart);

    @Nullable
    AABB getBoundingBoxForCulling(AbstractMinecart cart);
}
