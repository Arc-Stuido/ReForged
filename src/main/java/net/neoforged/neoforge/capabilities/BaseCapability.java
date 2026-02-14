package net.neoforged.neoforge.capabilities;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's BaseCapability — abstract base for all capability types.
 */
public abstract class BaseCapability<T, C extends @Nullable Object> {
    private final ResourceLocation name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;

    protected BaseCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
    }

    public final ResourceLocation name() { return name; }
    public final Class<T> typeClass() { return typeClass; }
    public final Class<C> contextClass() { return contextClass; }
}
