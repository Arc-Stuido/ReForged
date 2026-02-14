package net.neoforged.neoforge.event.entity;

import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired on the <b>mod event bus</b> to allow mods to register custom entity attributes.
 *
 * <p>NeoForge mods call {@link #put(EntityType, AttributeSupplier)} to register attributes
 * for their entity types. The backing map is shared with Forge's attribute map so that
 * registrations are visible to {@code DefaultAttributes}.</p>
 */
public class EntityAttributeCreationEvent extends Event implements IModBusEvent {

    private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes;

    public EntityAttributeCreationEvent(Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes) {
        this.attributes = attributes;
    }

    /**
     * Register an {@link AttributeSupplier} for the given entity type.
     *
     * @throws IllegalArgumentException if the entity type already has attributes registered
     */
    public void put(EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
        if (attributes.containsKey(entity)) {
            throw new IllegalArgumentException("Duplicate entity attribute registration for " + entity);
        }
        attributes.put(entity, map);
    }
}
