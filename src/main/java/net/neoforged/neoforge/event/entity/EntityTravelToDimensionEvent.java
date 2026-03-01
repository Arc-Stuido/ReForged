package net.neoforged.neoforge.event.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an entity travels to another dimension.
 */
public class EntityTravelToDimensionEvent extends Event {
    private final Entity entity;
    private final ResourceKey<Level> dimension;

    public EntityTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
        this.entity = entity;
        this.dimension = dimension;
    }

    public Entity getEntity() { return entity; }
    public ResourceKey<Level> getDimension() { return dimension; }
}
