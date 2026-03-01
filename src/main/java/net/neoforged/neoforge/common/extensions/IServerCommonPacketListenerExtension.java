package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Stub extension for ServerCommonPacketListener.
 */
public interface IServerCommonPacketListenerExtension extends ICommonPacketListener {
    default void send(CustomPacketPayload payload) {}
}
