package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Base event for item entity events.
 */
public class ItemEvent extends Event {
    private final ItemEntity entityItem;

    public ItemEvent(ItemEntity entityItem) {
        this.entityItem = entityItem;
    }

    public ItemEntity getEntity() {
        return entityItem;
    }
}
