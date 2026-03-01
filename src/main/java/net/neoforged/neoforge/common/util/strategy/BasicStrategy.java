package net.neoforged.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;

/**
 * Basic hashing strategy using standard equals/hashCode.
 */
public enum BasicStrategy implements Hash.Strategy<Object> {
    BASIC;

    @Override
    public int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object a, Object b) {
        return java.util.Objects.equals(a, b);
    }
}
