package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * Proxy: NeoForge's IPayloadContext — provides context for handling network payloads.
 */
public interface IPayloadContext {

    /**
     * {@return the network connection}
     */
    Connection connection();

    /**
     * Retrieves the player relevant to this payload.
     * For server-bound payloads, returns the sending ServerPlayer.
     * For client-bound payloads, returns the receiving LocalPlayer.
     */
    Player player();

    /**
     * Sends the given payload back to the sender.
     */
    void reply(CustomPacketPayload payload);

    /**
     * Disconnects the player from the network.
     */
    void disconnect(Component reason);

    /**
     * Submits the given task to run on the main thread.
     */
    CompletableFuture<Void> enqueueWork(Runnable task);

    /**
     * Submits the given task to run on the main thread and returns its result.
     */
    <T> CompletableFuture<T> enqueueWork(Supplier<T> task);

    /**
     * {@return the flow of the received payload}
     */
    PacketFlow flow();

    /**
     * {@return the protocol of the connection}
     */
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    /**
     * Handles a payload using the current context.
     */
    default void handle(CustomPacketPayload payload) {
        // Default no-op for compatibility
    }
}
