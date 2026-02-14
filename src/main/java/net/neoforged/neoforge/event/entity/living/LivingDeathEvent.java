package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDeathEvent}. */
public class LivingDeathEvent {
    private final net.minecraftforge.event.entity.living.LivingDeathEvent delegate;

    public LivingDeathEvent(net.minecraftforge.event.entity.living.LivingDeathEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }
    public DamageSource getSource() { return delegate.getSource(); }
}
