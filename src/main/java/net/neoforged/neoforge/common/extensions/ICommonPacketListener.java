package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.Packet;

/**
 * Stub: Common packet listener extension.
 */
public interface ICommonPacketListener {
    default void send(Packet<?> packet) {}
}
