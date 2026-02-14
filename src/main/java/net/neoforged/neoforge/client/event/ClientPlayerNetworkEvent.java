package net.neoforged.neoforge.client.event;

public class ClientPlayerNetworkEvent extends net.neoforged.bus.api.Event {

    public static class LoggingIn extends ClientPlayerNetworkEvent {
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
    }
}
