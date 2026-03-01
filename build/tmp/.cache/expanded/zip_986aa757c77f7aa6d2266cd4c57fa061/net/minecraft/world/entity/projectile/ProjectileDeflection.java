package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
    ProjectileDeflection NONE = (p_335766_, p_335741_, p_334113_) -> {
    };
    ProjectileDeflection REVERSE = (p_341488_, p_341489_, p_341490_) -> {
        float f = 170.0F + p_341490_.nextFloat() * 20.0F;
        p_341488_.setDeltaMovement(p_341488_.getDeltaMovement().scale(-0.5));
        p_341488_.setYRot(p_341488_.getYRot() + f);
        p_341488_.yRotO += f;
        p_341488_.hasImpulse = true;
    };
    ProjectileDeflection AIM_DEFLECT = (p_341485_, p_341486_, p_341487_) -> {
        if (p_341486_ != null) {
            Vec3 vec3 = p_341486_.getLookAngle().normalize();
            p_341485_.setDeltaMovement(vec3);
            p_341485_.hasImpulse = true;
        }
    };
    ProjectileDeflection MOMENTUM_DEFLECT = (p_341482_, p_341483_, p_341484_) -> {
        if (p_341483_ != null) {
            Vec3 vec3 = p_341483_.getDeltaMovement().normalize();
            p_341482_.setDeltaMovement(vec3);
            p_341482_.hasImpulse = true;
        }
    };

    void deflect(Projectile p_332034_, @Nullable Entity p_330319_, RandomSource p_333938_);
}