package net.neoforged.neoforge.client.event;

/**
 * NeoForge client tick events.
 * Wraps Forge's {@link net.minecraftforge.event.TickEvent.ClientTickEvent} sub-events.
 */
public class ClientTickEvent extends net.neoforged.bus.api.Event {

    /**
     * Fires at the start of every client tick.
     * Wraps Forge's {@link net.minecraftforge.event.TickEvent.ClientTickEvent.Pre}.
     */
    public static class Pre extends ClientTickEvent {
        @SuppressWarnings("unused")
        private final net.minecraftforge.event.TickEvent.ClientTickEvent.Pre delegate;

        /** Wrapper constructor for Forge event bridging. */
        public Pre(net.minecraftforge.event.TickEvent.ClientTickEvent.Pre delegate) {
            this.delegate = delegate;
        }
    }

    /**
     * Fires at the end of every client tick.
     * Wraps Forge's {@link net.minecraftforge.event.TickEvent.ClientTickEvent.Post}.
     */
    public static class Post extends ClientTickEvent {
        @SuppressWarnings("unused")
        private final net.minecraftforge.event.TickEvent.ClientTickEvent.Post delegate;

        /** Wrapper constructor for Forge event bridging. */
        public Post(net.minecraftforge.event.TickEvent.ClientTickEvent.Post delegate) {
            this.delegate = delegate;
        }
    }
}
