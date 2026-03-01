package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Proxy: NeoForge's DirectionalPayloadHandler — wraps separate client and server handlers.
 */
public record DirectionalPayloadHandler<T extends CustomPacketPayload>(IPayloadHandler<T> clientHandler, IPayloadHandler<T> serverHandler) implements IPayloadHandler<T> {

    @Override
    public void handle(T payload, IPayloadContext context) {
        if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
            if (clientHandler != null) {
                clientHandler.handle(payload, context);
            }
        } else {
            if (serverHandler != null) {
                serverHandler.handle(payload, context);
            }
        }
    }
}
