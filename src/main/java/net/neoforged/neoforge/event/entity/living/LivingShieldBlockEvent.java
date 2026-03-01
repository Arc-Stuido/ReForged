package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Stub: Fired when a living entity shields a blow.
 */
public class LivingShieldBlockEvent extends LivingEvent {
    private final DamageSource damageSource;
    private float blockedDamage;
    private boolean shieldTakesDamage = true;

    public LivingShieldBlockEvent(LivingEntity entity, DamageSource damageSource, float blockedDamage) {
        super(entity);
        this.damageSource = damageSource;
        this.blockedDamage = blockedDamage;
    }

    public DamageSource getDamageSource() { return damageSource; }
    public float getBlockedDamage() { return blockedDamage; }
    public void setBlockedDamage(float blockedDamage) { this.blockedDamage = blockedDamage; }
    public boolean shieldTakesDamage() { return shieldTakesDamage; }
    public void setShieldTakesDamage(boolean shieldTakesDamage) { this.shieldTakesDamage = shieldTakesDamage; }
}
