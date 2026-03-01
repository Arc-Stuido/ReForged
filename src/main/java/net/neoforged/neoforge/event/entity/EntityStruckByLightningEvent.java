package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an entity is struck by lightning.
 */
public class EntityStruckByLightningEvent extends Event {
    private final Entity entity;
    private final LightningBolt lightning;

    public EntityStruckByLightningEvent(Entity entity, LightningBolt lightning) {
        this.entity = entity;
        this.lightning = lightning;
    }

    public Entity getEntity() { return entity; }
    public LightningBolt getLightning() { return lightning; }
}
