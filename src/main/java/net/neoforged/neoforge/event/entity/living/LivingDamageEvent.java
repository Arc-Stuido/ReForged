package net.neoforged.neoforge.event.entity.living;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDamageEvent}.
 * NeoForge has Pre/Post subclasses; Forge just has the base event.
 */
public abstract class LivingDamageEvent extends LivingEvent {
    private final net.minecraftforge.event.entity.living.LivingDamageEvent delegate;

    public LivingDamageEvent() {
        super();
        this.delegate = null;
    }

    public LivingDamageEvent(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
        super(delegate.getEntity());
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public void setAmount(float amount) { delegate.setAmount(amount); }
    public DamageSource getSource() { return delegate.getSource(); }

    /** NeoForge Pre fires at same time as Forge's LivingDamageEvent. */
    public static class Pre extends LivingDamageEvent {
        private final DamageContainer container;

        public Pre(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
            super(delegate);
            this.container = new DamageContainer(delegate.getSource(), delegate.getAmount());
        }

        public DamageContainer getContainer() { return container; }
        public float getNewDamage() { return container.getNewDamage(); }
        public float getOriginalDamage() { return container.getOriginalDamage(); }
        public void setNewDamage(float newDamage) {
            container.setNewDamage(newDamage);
            setAmount(newDamage);
        }
    }

    public static class Post extends LivingDamageEvent {
        private final DamageContainer container;

        public Post(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
            super(delegate);
            this.container = new DamageContainer(delegate.getSource(), delegate.getAmount());
            this.container.setNewDamage(delegate.getAmount());
        }

        public float getOriginalDamage() { return container.getOriginalDamage(); }
        public float getNewDamage() { return container.getNewDamage(); }
        public float getBlockedDamage() { return container.getBlockedDamage(); }
        public float getShieldDamage() { return container.getShieldDamage(); }
        public int getPostAttackInvulnerabilityTicks() { return container.getPostAttackInvulnerabilityTicks(); }
        public float getReduction(DamageContainer.Reduction reduction) { return container.getReduction(reduction); }
    }
}
