package net.neoforged.neoforge.server.permission.nodes;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Shim: NeoForge PermissionNode — represents a permission in the permission system.
 */
public final class PermissionNode<T> {
    private final String nodeName;
    private final PermissionType<T> type;
    private final PermissionResolver<T> defaultResolver;
    private final PermissionDynamicContextKey<?>[] dynamics;

    @Nullable
    private Component readableName;
    @Nullable
    private Component description;

    public PermissionNode(ResourceLocation nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) {
        this(nodeName.getNamespace(), nodeName.getPath(), type, defaultResolver, dynamics);
    }

    public PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) {
        this(modID + "." + nodeName, type, defaultResolver, dynamics);
    }

    private PermissionNode(String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) {
        this.nodeName = nodeName;
        this.type = type;
        this.dynamics = dynamics;
        this.defaultResolver = defaultResolver;
    }

    public PermissionNode<T> setInformation(Component readableName, Component description) {
        this.readableName = readableName;
        this.description = description;
        return this;
    }

    public String getNodeName() { return nodeName; }
    public PermissionType<T> getType() { return type; }
    public PermissionDynamicContextKey<?>[] getDynamics() { return dynamics; }
    public PermissionResolver<T> getDefaultResolver() { return defaultResolver; }
    @Nullable public Component getReadableName() { return readableName; }
    @Nullable public Component getDescription() { return description; }

    @FunctionalInterface
    public interface PermissionResolver<T> {
        T resolve(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionNode<?> otherNode)) return false;
        return nodeName.equals(otherNode.nodeName) && type.equals(otherNode.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeName, type);
    }
}
