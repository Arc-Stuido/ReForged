package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingHealEvent}. */
public class LivingHealEvent {
    private final net.minecraftforge.event.entity.living.LivingHealEvent delegate;

    public LivingHealEvent(net.minecraftforge.event.entity.living.LivingHealEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public void setAmount(float amount) { delegate.setAmount(amount); }
}
