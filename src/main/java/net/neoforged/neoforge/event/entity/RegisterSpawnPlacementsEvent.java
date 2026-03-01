package net.neoforged.neoforge.event.entity;

import net.neoforged.fml.event.IModBusEvent;
public class RegisterSpawnPlacementsEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    public enum Operation { AND, OR, REPLACE }
}
