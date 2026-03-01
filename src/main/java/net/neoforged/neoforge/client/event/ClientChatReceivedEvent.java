package net.neoforged.neoforge.client.event;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a chat message is received on the client.
 */
public class ClientChatReceivedEvent extends Event {
    private Component message;

    public ClientChatReceivedEvent(Component message) {
        this.message = message;
    }

    public Component getMessage() { return message; }
    public void setMessage(Component message) { this.message = message; }

    public static class System extends ClientChatReceivedEvent {
        public System(Component message) { super(message); }
    }

    public static class Player extends ClientChatReceivedEvent {
        public Player(Component message) { super(message); }
    }
}
