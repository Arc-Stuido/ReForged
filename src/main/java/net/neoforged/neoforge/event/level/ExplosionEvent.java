package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/** Wrapper around Forge's {@link net.minecraftforge.event.level.ExplosionEvent}. */
public class ExplosionEvent {
    private final net.minecraftforge.event.level.ExplosionEvent delegate;

    public ExplosionEvent(net.minecraftforge.event.level.ExplosionEvent delegate) {
        this.delegate = delegate;
    }

    public Explosion getExplosion() { return delegate.getExplosion(); }
    public Level getLevel() { return delegate.getLevel(); }

    public static class Start extends ExplosionEvent {
        public Start(net.minecraftforge.event.level.ExplosionEvent.Start delegate) {
            super(delegate);
        }
    }

    public static class Detonate {
    }
}
