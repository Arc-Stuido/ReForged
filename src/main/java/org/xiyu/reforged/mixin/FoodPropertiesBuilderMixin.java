package org.xiyu.reforged.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

/**
 * Adds NeoForge's {@code effect(Supplier<MobEffectInstance>, float)} method
 * to vanilla's {@link FoodProperties.Builder}.
 */
@Mixin(FoodProperties.Builder.class)
public abstract class FoodPropertiesBuilderMixin {

    @Shadow
    public abstract FoodProperties.Builder effect(MobEffectInstance effect, float probability);

    /**
     * NeoForge extension: accept a Supplier of MobEffectInstance.
     * Simply calls .get() and delegates to the vanilla method.
     */
    public FoodProperties.Builder effect(Supplier<MobEffectInstance> effectSupplier, float probability) {
        return this.effect(effectSupplier.get(), probability);
    }
}
