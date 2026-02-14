package net.neoforged.fml.event;

/**
 * NeoForge IModBusEvent — marker interface for events posted on the mod event bus.
 *
 * <p>Extends Forge's {@code IModBusEvent} so that NeoForge mod-bus events pass
 * the Forge event bus's type validation when registered via
 * {@code delegate.addListener()}.</p>
 */
public interface IModBusEvent extends net.minecraftforge.fml.event.IModBusEvent {
}
