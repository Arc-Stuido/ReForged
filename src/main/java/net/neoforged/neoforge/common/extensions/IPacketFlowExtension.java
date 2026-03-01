package net.neoforged.neoforge.common.extensions;

/**
 * Stub extension for PacketFlow.
 */
public interface IPacketFlowExtension {
    default boolean isClientbound() { return false; }
    default boolean isServerbound() { return false; }
}
