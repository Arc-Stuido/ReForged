package net.neoforged.neoforge.network.connection;

/**
 * Connection type enum for distinguishing NeoForge vs vanilla connections.
 */
public enum ConnectionType {
    NEOFORGE,
    VANILLA,
    OTHER;

    public boolean isNeoForge() {
        return this == NEOFORGE;
    }

    public boolean isVanilla() {
        return this == VANILLA;
    }
}
