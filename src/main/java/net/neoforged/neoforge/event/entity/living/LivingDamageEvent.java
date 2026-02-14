package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDamageEvent}.
 * NeoForge has Pre/Post subclasses; Forge just has the base event.
 */
public class LivingDamageEvent {
    private final net.minecraftforge.event.entity.living.LivingDamageEvent delegate;

    public LivingDamageEvent() { this.delegate = null; }

    public LivingDamageEvent(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public void setAmount(float amount) { delegate.setAmount(amount); }
    public DamageSource getSource() { return delegate.getSource(); }

    /** NeoForge Pre fires at same time as Forge's LivingDamageEvent. */
    public static class Pre extends LivingDamageEvent {
        public Pre(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
            super(delegate);
        }
    }

    public static class Post extends LivingDamageEvent {
    }
}
