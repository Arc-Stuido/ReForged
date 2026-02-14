package net.neoforged.neoforge.event.level;

public class BlockEvent extends net.neoforged.bus.api.Event {

    public static class BreakEvent extends BlockEvent {
    }

    public static class EntityMultiPlaceEvent extends BlockEvent {
    }

    public static class EntityPlaceEvent extends BlockEvent {
    }
}
