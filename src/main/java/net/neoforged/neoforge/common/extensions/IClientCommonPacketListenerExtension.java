package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Stub extension for ClientCommonPacketListener.
 */
public interface IClientCommonPacketListenerExtension extends ICommonPacketListener {
    default void send(CustomPacketPayload payload) {}
}
