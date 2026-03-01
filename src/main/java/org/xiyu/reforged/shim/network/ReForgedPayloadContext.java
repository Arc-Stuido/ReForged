package org.xiyu.reforged.shim.network;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Bridges NeoForge's {@code IPayloadContext} to Forge's {@code CustomPayloadEvent.Context}.
 *
 * <p>This implementation wraps a Forge context and delegates all calls appropriately,
 * providing NeoForge mods with the context interface they expect.</p>
 */
public class ReForgedPayloadContext implements IPayloadContext {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final CustomPayloadEvent.Context forgeContext;
    private final PacketFlow flow;

    public ReForgedPayloadContext(CustomPayloadEvent.Context forgeContext, PacketFlow flow) {
        this.forgeContext = forgeContext;
        this.flow = flow;
    }

    /**
     * Convenience constructor that auto-detects flow from the connection.
     */
    public ReForgedPayloadContext(CustomPayloadEvent.Context forgeContext) {
        this.forgeContext = forgeContext;
        // If we are on the client side, this packet was received from server (CLIENTBOUND)
        // If we are on the server side, this packet was received from client (SERVERBOUND)
        this.flow = forgeContext.isClientSide() ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND;
    }

    @Override
    public Connection connection() {
        return forgeContext.getConnection();
    }

    @Override
    public Player player() {
        // Server-bound: getSender() returns the ServerPlayer that sent the packet
        ServerPlayer sender = forgeContext.getSender();
        if (sender != null) {
            return sender;
        }
        // Client-bound: return the local player
        try {
            return Minecraft.getInstance().player;
        } catch (Throwable e) {
            LOGGER.warn("[ReForged] Unable to get player from payload context");
            return null;
        }
    }

    @Override
    public void reply(CustomPacketPayload payload) {
        // Reply sends back through the same connection
        // For now, we use the PayloadChannelRegistry to find the channel and send
        LOGGER.debug("[ReForged] IPayloadContext.reply() called with payload: {}", payload.type().id());
        PayloadChannelRegistry.sendViaConnection(connection(), payload);
    }

    @Override
    public void disconnect(Component reason) {
        connection().disconnect(reason);
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable task) {
        return forgeContext.enqueueWork(task);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        // Forge's Context only has enqueueWork(Runnable), so we wrap the Supplier
        CompletableFuture<T> future = new CompletableFuture<>();
        forgeContext.enqueueWork(() -> {
            try {
                future.complete(task.get());
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    @Override
    public PacketFlow flow() {
        return flow;
    }

    /**
     * Access the underlying Forge context for advanced operations.
     */
    public CustomPayloadEvent.Context getForgeContext() {
        return forgeContext;
    }
}
