package net.neoforged.neoforge.registries;

import net.neoforged.fml.event.IModBusEvent;
public class DataPackRegistryEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    public static class NewRegistry extends DataPackRegistryEvent {
    }
}
