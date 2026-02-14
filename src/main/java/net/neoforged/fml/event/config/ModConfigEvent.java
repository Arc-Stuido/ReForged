package net.neoforged.fml.event.config;

/**
 * Wrapper around Forge's {@link net.minecraftforge.fml.event.config.ModConfigEvent}.
 * Champions uses {@code @SubscribeEvent onConfigLoad(ModConfigEvent event)}.
 */
public class ModConfigEvent {
    private final net.minecraftforge.fml.event.config.ModConfigEvent delegate;

    public ModConfigEvent(net.minecraftforge.fml.event.config.ModConfigEvent delegate) {
        this.delegate = delegate;
    }

    public net.neoforged.fml.config.ModConfig getConfig() {
        // Wrap Forge's ModConfig into our NeoForge ModConfig shim
        return new net.neoforged.fml.config.ModConfig(delegate.getConfig());
    }

    public static class Loading extends ModConfigEvent {
        public Loading(net.minecraftforge.fml.event.config.ModConfigEvent delegate) { super(delegate); }
    }

    public static class Reloading extends ModConfigEvent {
        public Reloading(net.minecraftforge.fml.event.config.ModConfigEvent delegate) { super(delegate); }
    }
}
