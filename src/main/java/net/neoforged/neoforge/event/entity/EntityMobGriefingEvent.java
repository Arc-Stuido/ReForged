package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to check if an entity can grief blocks in the world.
 */
public class EntityMobGriefingEvent extends Event {
    private final Entity entity;

    public EntityMobGriefingEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() { return entity; }
}
