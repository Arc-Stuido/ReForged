package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Collections;

/**
 * Stub: A linked hash set that supports insertion at arbitrary positions.
 */
public class InsertableLinkedOpenCustomHashSet<T> extends AbstractSet<T> {
    public InsertableLinkedOpenCustomHashSet(Hash.Strategy<? super T> strategy) {
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.<T>emptySet().iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T t) {
        return false;
    }
}
