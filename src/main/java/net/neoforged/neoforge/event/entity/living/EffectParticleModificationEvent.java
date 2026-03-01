package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to modify effect particles on a living entity.
 */
public class EffectParticleModificationEvent extends Event {
    private final LivingEntity entity;

    public EffectParticleModificationEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() { return entity; }
}
