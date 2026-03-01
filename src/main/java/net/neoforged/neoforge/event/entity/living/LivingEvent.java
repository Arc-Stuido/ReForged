package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/**
 * Base living event — provides entity access for all living sub-events.
 */
public class LivingEvent extends net.neoforged.bus.api.Event {
    private final LivingEntity entity;

    public LivingEvent() {
        this.entity = null;
    }

    public LivingEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public static class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent() { super(); }
        public LivingJumpEvent(LivingEntity entity) { super(entity); }
    }

    public static class LivingVisibilityEvent extends LivingEvent {
        public LivingVisibilityEvent() { super(); }
        public LivingVisibilityEvent(LivingEntity entity) { super(entity); }
    }
}
