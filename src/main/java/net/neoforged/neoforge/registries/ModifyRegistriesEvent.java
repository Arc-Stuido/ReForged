package net.neoforged.neoforge.registries;

import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to allow modification of registries before they are frozen.
 */
public class ModifyRegistriesEvent extends Event implements IModBusEvent {
    public ModifyRegistriesEvent() {
    }
}
