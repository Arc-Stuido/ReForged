package net.neoforged.neoforge.common;

import net.minecraftforge.common.MinecraftForge;
import net.neoforged.bus.api.IEventBus;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

/**
 * Proxy for NeoForge's main class.
 * NeoForge mods access {@code NeoForge.EVENT_BUS} — we redirect to Forge's bus
 * through {@link NeoForgeEventBusAdapter} so that NeoForge {@code @SubscribeEvent}
 * annotations are properly scanned and wrapper events are bridged.
 */
public final class NeoForge {
    /**
     * The game-wide event bus. Wraps {@link MinecraftForge#EVENT_BUS} via our adapter
     * which handles NeoForge annotation scanning and event wrapping.
     */
    public static final IEventBus EVENT_BUS = NeoForgeEventBusAdapter.wrap(MinecraftForge.EVENT_BUS);

    private NeoForge() {}
}
