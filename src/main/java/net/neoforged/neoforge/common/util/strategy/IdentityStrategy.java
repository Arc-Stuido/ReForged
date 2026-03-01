package net.neoforged.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;

/**
 * Identity hashing strategy using == and System.identityHashCode.
 */
public enum IdentityStrategy implements Hash.Strategy<Object> {
    IDENTITY;

    @Override
    public int hashCode(Object o) {
        return System.identityHashCode(o);
    }

    @Override
    public boolean equals(Object a, Object b) {
        return a == b;
    }
}
