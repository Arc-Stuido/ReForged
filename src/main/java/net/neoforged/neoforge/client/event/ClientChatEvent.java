package net.neoforged.neoforge.client.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a player sends a chat message from the client.
 */
public class ClientChatEvent extends Event {
    private String message;

    public ClientChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
