package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/**
 * Stub: Fired when a living entity is drowning.
 */
public class LivingDrownEvent extends LivingEvent {
    private boolean isDrowning;

    public LivingDrownEvent(LivingEntity entity, boolean isDrowning) {
        super(entity);
        this.isDrowning = isDrowning;
    }

    public boolean isDrowning() { return isDrowning; }
    public void setDrowning(boolean drowning) { this.isDrowning = drowning; }
}
