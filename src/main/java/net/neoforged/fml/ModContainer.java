package net.neoforged.fml;

/**
 * Proxy for NeoForge's {@code ModContainer}.
 * NeoForge mods may receive a ModContainer in their constructor.
 *
 * <p>Since Forge's ModContainer is abstract with complex constructor requirements,
 * we use composition. NeoForge mods that reference {@code net.neoforged.fml.ModContainer}
 * will find this class, and the Mixin in FMLModContainer handles passing the
 * correct container to the mod constructor.</p>
 */
public class ModContainer {

    private static net.neoforged.bus.api.IEventBus globalModBus;

    private final net.minecraftforge.fml.ModContainer delegate;
    private net.neoforged.bus.api.IEventBus eventBus;

    public ModContainer(net.minecraftforge.fml.ModContainer delegate) {
        this.delegate = delegate;
        this.eventBus = globalModBus;
    }

    /**
     * Set the global mod event bus used by all NeoForge ModContainer wrappers.
     * Called once during NeoForgeModLoader initialization.
     */
    public static void setGlobalModBus(net.neoforged.bus.api.IEventBus bus) {
        globalModBus = bus;
    }

    /**
     * Get the mod event bus for this container.
     * NeoForge mods (e.g. TwilightForest BeanContext) call this to register listeners.
     */
    public net.neoforged.bus.api.IEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get the mod ID.
     */
    public String getModId() {
        return delegate.getModId();
    }

    /**
     * Get the namespace (same as mod ID in most cases).
     */
    public String getNamespace() {
        return delegate.getModId();
    }

    /**
     * Get the mod info.
     * Returns NeoForge's IModInfo wrapping the underlying Forge info.
     */
    public net.neoforged.neoforgespi.language.IModInfo getModInfo() {
        return net.neoforged.neoforgespi.language.IModInfo.wrap(delegate.getModInfo());
    }

    /**
     * Get the underlying Forge container.
     */
    public net.minecraftforge.fml.ModContainer getForgeContainer() {
        return delegate;
    }

    /**
     * Wrap a Forge ModContainer for NeoForge mod constructors.
     */
    public static ModContainer wrap(net.minecraftforge.fml.ModContainer forgeContainer) {
        return new ModContainer(forgeContainer);
    }

    public static ModContainer wrap(net.minecraftforge.fml.ModContainer forgeContainer, net.neoforged.bus.api.IEventBus bus) {
        ModContainer mc = new ModContainer(forgeContainer);
        mc.eventBus = bus;
        return mc;
    }

    /**
     * Register a config spec with this mod container.
     * NeoForge mods call this to associate configs with their mod.
     */
    public void registerConfig(net.neoforged.fml.config.ModConfig.Type type,
                               net.neoforged.fml.config.IConfigSpec spec) {
        // Convert NeoForge ModConfig.Type to Forge ModConfig.Type
        net.minecraftforge.fml.config.ModConfig.Type forgeType = switch (type) {
            case COMMON -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
            case STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON; // Fallback
        };

        // delegate is the Forge ModContainer set as active during mod construction.
        // When NeoForge mods are constructed, this will be the NeoModContainer with the
        // correct modId, so configs get the correct filename (e.g. "champions-common.toml").
        if (spec instanceof net.neoforged.neoforge.common.ModConfigSpec mcs) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, mcs.getForgeSpec(), delegate));
        }
    }

    /**
     * Register a config spec with a custom filename.
     */
    public void registerConfig(net.neoforged.fml.config.ModConfig.Type type,
                               net.neoforged.fml.config.IConfigSpec spec, String fileName) {
        net.minecraftforge.fml.config.ModConfig.Type forgeType = switch (type) {
            case COMMON -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
            case STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
        };

        if (spec instanceof net.neoforged.neoforge.common.ModConfigSpec mcs) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, mcs.getForgeSpec(), delegate, fileName));
        }
    }
}
