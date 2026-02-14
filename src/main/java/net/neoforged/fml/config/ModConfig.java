package net.neoforged.fml.config;

import net.minecraftforge.fml.ModContainer;

/**
 * Proxy: NeoForge's ModConfig.
 * Wraps Forge's config system.
 */
public class ModConfig {
    public enum Type {
        COMMON, CLIENT, SERVER, STARTUP
    }

    private final Type type;
    private final Object spec;
    private final String modId;

    public ModConfig(Type type, Object spec, ModContainer container) {
        this.type = type;
        this.spec = spec;
        this.modId = container.getModId();
    }

    public ModConfig(Type type, Object spec, ModContainer container, String fileName) {
        this(type, spec, container);
    }

    /** Wrap a Forge ModConfig into our NeoForge shim. */
    public ModConfig(net.minecraftforge.fml.config.ModConfig forgeConfig) {
        this.type = Type.valueOf(forgeConfig.getType().name());
        this.spec = forgeConfig.getSpec();
        this.modId = forgeConfig.getModId();
    }

    public Type getType() { return type; }
    public Object getSpec() { return spec; }
    public String getModId() { return modId; }
    public String getFileName() { return modId + "-" + type.name().toLowerCase() + ".toml"; }
}
