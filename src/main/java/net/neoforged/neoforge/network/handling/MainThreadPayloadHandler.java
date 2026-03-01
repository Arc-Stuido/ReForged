package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Proxy: NeoForge's MainThreadPayloadHandler — wraps a handler to ensure it runs on the main thread.
 * In our Forge bridge, this is a passthrough since we use consumerMainThread.
 */
public record MainThreadPayloadHandler<T extends CustomPacketPayload>(IPayloadHandler<T> handler) implements IPayloadHandler<T> {

    @Override
    public void handle(T payload, IPayloadContext context) {
        context.enqueueWork(() -> handler.handle(payload, context));
    }
}
