package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingConversionEvent}. */
public class LivingConversionEvent {
    private final net.minecraftforge.event.entity.living.LivingConversionEvent delegate;

    public LivingConversionEvent(net.minecraftforge.event.entity.living.LivingConversionEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }

    public static class Post extends LivingConversionEvent {
        private final net.minecraftforge.event.entity.living.LivingConversionEvent.Post forgeEvent;

        public Post(net.minecraftforge.event.entity.living.LivingConversionEvent.Post delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public LivingEntity getOutcome() { return forgeEvent.getOutcome(); }
    }
}
