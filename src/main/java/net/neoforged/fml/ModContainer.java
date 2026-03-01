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
        net.minecraftforge.fml.config.ModConfig.Type forgeType = toForgeType(type);
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpec(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate));
        }
    }

    /**
     * Register an extension point with this mod container.
     * NeoForge mods use this for config screen factories, display tests, etc.
     * In our bridge this is a no-op since Forge uses a different mechanism.
     */
    public <T extends IExtensionPoint> void registerExtensionPoint(Class<T> point, T extension) {
        // No-op: Forge handles extension points differently
    }

    /**
     * Register a config spec with a custom filename.
     */
    public void registerConfig(net.neoforged.fml.config.ModConfig.Type type,
                               net.neoforged.fml.config.IConfigSpec spec, String fileName) {
        net.minecraftforge.fml.config.ModConfig.Type forgeType = toForgeType(type);
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpec(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate, fileName));
        }
    }

    private static net.minecraftforge.fml.config.ModConfig.Type toForgeType(net.neoforged.fml.config.ModConfig.Type type) {
        return switch (type) {
            case COMMON -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
            case STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
        };
    }

    private net.minecraftforge.common.ForgeConfigSpec toForgeConfigSpec(net.neoforged.fml.config.IConfigSpec spec) {
        if (spec == null) {
            return null;
        }
        if (spec instanceof net.neoforged.neoforge.common.ModConfigSpec mcs) {
            return mcs.getForgeSpec();
        }
        if (spec instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
            return fcs;
        }

        try {
            java.lang.reflect.Method method = spec.getClass().getMethod("getForgeSpec");
            Object value = method.invoke(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        try {
            java.lang.reflect.Method method = spec.getClass().getMethod("getSpec");
            Object value = method.invoke(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        try {
            java.lang.reflect.Field field = spec.getClass().getDeclaredField("forgeSpec");
            field.setAccessible(true);
            Object value = field.get(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        org.slf4j.LoggerFactory.getLogger(ModContainer.class).warn(
                "[ReForged] Unsupported NeoForge IConfigSpec implementation for mod '{}': {}",
                getModId(), spec.getClass().getName());
        return null;
    }
}
