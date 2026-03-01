package net.neoforged.neoforge.network.registration;

/**
 * Proxy: NeoForge's HandlerThread enum — specifies which thread a payload handler should execute on.
 */
public enum HandlerThread {
    /** Handle on the main game thread (default). */
    MAIN,
    /** Handle on the network thread. */
    NETWORK
}
