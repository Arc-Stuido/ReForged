package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Mob;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.MobSpawnEvent}. */
public class MobSpawnEvent {
    private final net.minecraftforge.event.entity.living.MobSpawnEvent delegate;

    public MobSpawnEvent(net.minecraftforge.event.entity.living.MobSpawnEvent delegate) {
        this.delegate = delegate;
    }

    public Mob getEntity() { return delegate.getEntity(); }

    public static class PositionCheck extends MobSpawnEvent {
        public PositionCheck(net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck delegate) {
            super(delegate);
        }
    }
}
