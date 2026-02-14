package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent}.
 * NeoForge has EntityTickEvent (all entities); Forge only has LivingTickEvent.
 */
public class EntityTickEvent {
    private final net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent delegate;

    public EntityTickEvent(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent delegate) {
        this.delegate = delegate;
    }

    public Entity getEntity() { return delegate.getEntity(); }

    /** Pre fires at same time as Forge's LivingTickEvent. */
    public static class Pre extends EntityTickEvent {
        public Pre(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent delegate) {
            super(delegate);
        }
    }

    public static class Post extends EntityTickEvent {
        public Post(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent delegate) {
            super(delegate);
        }
    }
}
