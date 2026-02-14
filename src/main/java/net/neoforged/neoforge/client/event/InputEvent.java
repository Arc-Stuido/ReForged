package net.neoforged.neoforge.client.event;

public class InputEvent extends net.neoforged.bus.api.Event {

    public static class InteractionKeyMappingTriggered extends InputEvent {
    }

    public static class Key extends InputEvent {
    }

    public static class MouseButton extends InputEvent {
        public static class Post extends MouseButton {
        }
        public static class Pre extends MouseButton {
        }
    }

    public static class MouseScrollingEvent extends InputEvent {
    }
}
