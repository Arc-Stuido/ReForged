package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingAttackEvent}.
 * NeoForge renamed LivingAttackEvent to LivingIncomingDamageEvent.
 */
public class LivingIncomingDamageEvent {
    private final net.minecraftforge.event.entity.living.LivingAttackEvent delegate;

    public LivingIncomingDamageEvent(net.minecraftforge.event.entity.living.LivingAttackEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public DamageSource getSource() { return delegate.getSource(); }
    /** NeoForge has setAmount; Forge's LivingAttackEvent doesn't — no-op. */
    public void setAmount(float amount) { /* not supported by Forge LivingAttackEvent */ }
}
