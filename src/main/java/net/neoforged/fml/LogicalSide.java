package net.neoforged.fml;

/**
 * Stub: NeoForge LogicalSide — represents a logical side (CLIENT or SERVER).
 */
public enum LogicalSide {
    CLIENT,
    SERVER;

    public boolean isClient() { return this == CLIENT; }
    public boolean isServer() { return this == SERVER; }
}
