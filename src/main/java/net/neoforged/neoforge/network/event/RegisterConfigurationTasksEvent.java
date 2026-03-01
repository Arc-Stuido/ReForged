package net.neoforged.neoforge.network.event;

import net.minecraftforge.eventbus.api.Event;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import java.util.function.Consumer;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Event for registering custom configuration tasks.
 */
public class RegisterConfigurationTasksEvent extends Event implements IModBusEvent {
    private final Consumer<ICustomConfigurationTask> registrar;

    public RegisterConfigurationTasksEvent(Consumer<ICustomConfigurationTask> registrar) {
        this.registrar = registrar;
    }

    public void register(ICustomConfigurationTask task) {
        registrar.accept(task);
    }
}
