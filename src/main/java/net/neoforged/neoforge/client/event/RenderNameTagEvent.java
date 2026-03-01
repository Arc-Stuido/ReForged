package net.neoforged.neoforge.client.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an entity's name tag is rendered.
 */
public class RenderNameTagEvent extends Event {
    private final Entity entity;
    private Component content;

    public RenderNameTagEvent(Entity entity, Component content) {
        this.entity = entity;
        this.content = content;
    }

    public Entity getEntity() { return entity; }
    public Component getContent() { return content; }
    public void setContent(Component content) { this.content = content; }
}
