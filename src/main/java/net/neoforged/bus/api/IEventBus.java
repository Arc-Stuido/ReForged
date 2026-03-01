package net.neoforged.bus.api;

/**
 * Proxy for NeoForge's {@code IEventBus}.
 * Simply re-exports Forge's IEventBus so NeoForge mods' references resolve.
 *
 * <p>Since this interface extends Forge's IEventBus, any object implementing
 * Forge's IEventBus is also an instance of this interface. Method calls
 * from NeoForge mod bytecode (e.g. {@code INVOKEINTERFACE net/neoforged/bus/api/IEventBus.addListener})
 * will resolve through the inheritance chain to Forge's implementation.</p>
 */
public interface IEventBus extends net.minecraftforge.eventbus.api.IEventBus {

    /**
     * NeoForge overload: post(NeoForge Event) returns the same event instance.
     */
    default net.neoforged.bus.api.Event post(net.neoforged.bus.api.Event event) {
        post((net.minecraftforge.eventbus.api.Event) event);
        return event;
    }

    /**
     * NeoForge overload: addListener(NeoForge EventPriority, Consumer).
     * Converts NeoForge priority to Forge priority and delegates.
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(EventPriority priority, java.util.function.Consumer<T> consumer) {
        addListener(priority.toForge(), consumer);
    }

    /**
     * NeoForge overload: addListener(NeoForge EventPriority, boolean, Consumer).
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(EventPriority priority, boolean receiveCancelled, java.util.function.Consumer<T> consumer) {
        addListener(priority.toForge(), receiveCancelled, consumer);
    }

    /**
     * NeoForge overload: addListener(NeoForge EventPriority, boolean, Class, Consumer).
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, java.util.function.Consumer<T> consumer) {
        addListener(priority.toForge(), receiveCancelled, eventType, consumer);
    }

    /**
     * NeoForge overload: addListener(Class, Consumer) — convenience with default priority.
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(Class<T> eventType, java.util.function.Consumer<T> consumer) {
        addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL, false, eventType, consumer);
    }

    /**
     * NeoForge overload: addListener(NeoForge EventPriority, Class, Consumer).
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(EventPriority priority, Class<T> eventType, java.util.function.Consumer<T> consumer) {
        addListener(priority.toForge(), false, eventType, consumer);
    }

    /**
     * NeoForge overload: addListener(boolean, Consumer).
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(boolean receiveCancelled, java.util.function.Consumer<T> consumer) {
        addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCancelled, consumer);
    }

    /**
     * NeoForge overload: addListener(boolean, Class, Consumer).
     */
    default <T extends net.minecraftforge.eventbus.api.Event> void addListener(boolean receiveCancelled, Class<T> eventType, java.util.function.Consumer<T> consumer) {
        addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCancelled, eventType, consumer);
    }
}
