package net.neoforged.neoforge.server.permission.nodes;

import java.util.Objects;

/**
 * Shim: NeoForge PermissionType — type wrapper for permission node values.
 */
public final class PermissionType<T> {
    private final Class<T> typeToken;
    private final String typeName;

    PermissionType(Class<T> typeToken, String typeName) {
        this.typeToken = typeToken;
        this.typeName = typeName;
    }

    public Class<T> typeToken() { return typeToken; }
    public String typeName() { return typeName; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PermissionType<?> otherType)) return false;
        return Objects.equals(this.typeToken, otherType.typeToken) &&
                Objects.equals(this.typeName, otherType.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeToken, typeName);
    }

    @Override
    public String toString() {
        return "PermissionType[typeToken=" + typeToken + ", typeName=" + typeName + ']';
    }
}
