package net.neoforged.neoforge.network.event;

import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/** Proxy: NeoForge RegisterPayloadHandlersEvent */
public class RegisterPayloadHandlersEvent extends Event implements IModBusEvent {
    public net.neoforged.neoforge.network.registration.PayloadRegistrar registrar(String version) {
        return new net.neoforged.neoforge.network.registration.PayloadRegistrar(version);
    }
}
