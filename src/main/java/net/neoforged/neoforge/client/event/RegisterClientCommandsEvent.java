package net.neoforged.neoforge.client.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to allow registration of client-side commands.
 */
public class RegisterClientCommandsEvent extends Event {
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public RegisterClientCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() { return dispatcher; }
}
