package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.EntityJoinLevelEvent}.
 */
public class EntityJoinLevelEvent {
    private final net.minecraftforge.event.entity.EntityJoinLevelEvent delegate;

    public EntityJoinLevelEvent(net.minecraftforge.event.entity.EntityJoinLevelEvent delegate) {
        this.delegate = delegate;
    }

    public Entity getEntity() { return delegate.getEntity(); }
    public Level getLevel() { return delegate.getLevel(); }
}
