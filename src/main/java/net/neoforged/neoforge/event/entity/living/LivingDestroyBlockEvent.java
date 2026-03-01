package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/**
 * Stub: Fired when a living entity destroys a block.
 */
public class LivingDestroyBlockEvent extends LivingEvent {
    public LivingDestroyBlockEvent(LivingEntity entity) {
        super(entity);
    }
}
