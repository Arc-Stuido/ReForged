package net.neoforged.neoforge.server.permission.nodes;

import java.util.function.Function;

/**
 * Shim: NeoForge PermissionDynamicContextKey — a key for building PermissionDynamicContext.
 */
public record PermissionDynamicContextKey<T>(Class<T> typeToken, String name, Function<T, String> serializer) {
    public PermissionDynamicContext<T> createContext(T value) {
        return new PermissionDynamicContext<>(this, value);
    }
}
