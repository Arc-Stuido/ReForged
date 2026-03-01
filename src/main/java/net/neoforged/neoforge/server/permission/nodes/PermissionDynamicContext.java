package net.neoforged.neoforge.server.permission.nodes;

import java.util.Objects;

/**
 * Shim: NeoForge PermissionDynamicContext — pair of a PermissionDynamicContextKey and a value.
 */
public final class PermissionDynamicContext<T> {
    private final PermissionDynamicContextKey<T> dynamic;
    private final T value;

    PermissionDynamicContext(PermissionDynamicContextKey<T> dynamic, T value) {
        this.dynamic = dynamic;
        this.value = value;
    }

    public PermissionDynamicContextKey<T> getDynamic() { return dynamic; }
    public T getValue() { return value; }

    public String getSerializedValue() {
        return this.dynamic.serializer().apply(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionDynamicContext<?> otherContext)) return false;
        return dynamic.equals(otherContext.dynamic) && value.equals(otherContext.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dynamic, value);
    }
}
