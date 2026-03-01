package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Function;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent}.
 */
public class RegisterClientTooltipComponentFactoriesEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public RegisterClientTooltipComponentFactoriesEvent(
            net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * Registers a tooltip component factory.
     */
    @SuppressWarnings("unchecked")
    public <T extends TooltipComponent> void register(
            Class<T> type,
            Function<? super T, ? extends ClientTooltipComponent> factory) {
        delegate.register(type, factory);
    }
}
