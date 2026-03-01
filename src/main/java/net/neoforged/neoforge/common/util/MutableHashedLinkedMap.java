package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import net.neoforged.neoforge.common.util.strategy.BasicStrategy;
import org.jetbrains.annotations.Nullable;

/**
 * A mutable linked map with a hashing strategy and a merge function.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public class MutableHashedLinkedMap<K, V> implements Iterable<Map.Entry<K, V>> {
    private final Strategy<? super K> strategy;
    private final Map<K, Entry> entries;
    private final MergeFunction<K, V> merge;
    private final BiPredicate<K, V> insertTest;
    private Entry head = null;
    private Entry last = null;
    private transient int changes = 0;

    public MutableHashedLinkedMap() {
        this(BasicStrategy.BASIC);
    }

    public MutableHashedLinkedMap(Strategy<? super K> strategy) {
        this(strategy, (k, v1, v2) -> v2);
    }

    public MutableHashedLinkedMap(Strategy<? super K> strategy, MergeFunction<K, V> merge) {
        this(strategy, merge, (k, v) -> true);
    }

    public MutableHashedLinkedMap(Strategy<? super K> strategy, BiPredicate<K, V> insertTest) {
        this(strategy, (k, v1, v2) -> v2, insertTest);
    }

    public MutableHashedLinkedMap(Strategy<? super K> strategy, MergeFunction<K, V> merge, BiPredicate<K, V> insertTest) {
        this.strategy = strategy;
        this.entries = new Object2ObjectOpenCustomHashMap<>(strategy);
        this.merge = merge;
        this.insertTest = insertTest;
    }

    @Nullable
    public V put(K key, V value) {
        if (!insertTest.test(key, value)) return null;
        Entry existing = entries.get(key);
        if (existing != null) {
            V old = existing.value;
            existing.value = merge.apply(key, old, value);
            return old;
        }
        Entry e = new Entry(key, value);
        entries.put(key, e);
        if (last == null) {
            head = last = e;
        } else {
            e.previous = last;
            last.next = e;
            last = e;
        }
        changes++;
        return null;
    }

    public boolean contains(K key) {
        return entries.containsKey(key);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Nullable
    public V remove(K key) {
        Entry e = entries.remove(key);
        if (e == null) return null;
        remove(e);
        return e.value;
    }

    @Nullable
    public V get(K key) {
        Entry e = entries.get(key);
        return e != null ? e.value : null;
    }

    @Nullable
    public V putFirst(K key, V value) {
        if (!insertTest.test(key, value)) return null;
        Entry existing = entries.get(key);
        if (existing != null) {
            V old = existing.value;
            existing.value = merge.apply(key, old, value);
            return old;
        }
        Entry e = new Entry(key, value);
        entries.put(key, e);
        if (head == null) {
            head = last = e;
        } else {
            e.next = head;
            head.previous = e;
            head = e;
        }
        changes++;
        return null;
    }

    @Nullable
    public V putAfter(K after, K key, V value) {
        if (!insertTest.test(key, value)) return null;
        Entry existing = entries.get(key);
        if (existing != null) {
            V old = existing.value;
            existing.value = merge.apply(key, old, value);
            return old;
        }
        Entry target = entries.get(after);
        if (target == null) return put(key, value);
        Entry e = new Entry(key, value);
        entries.put(key, e);
        e.previous = target;
        e.next = target.next;
        if (target.next != null) target.next.previous = e;
        else last = e;
        target.next = e;
        changes++;
        return null;
    }

    @Nullable
    public V putBefore(K before, K key, V value) {
        if (!insertTest.test(key, value)) return null;
        Entry existing = entries.get(key);
        if (existing != null) {
            V old = existing.value;
            existing.value = merge.apply(key, old, value);
            return old;
        }
        Entry target = entries.get(before);
        if (target == null) return put(key, value);
        Entry e = new Entry(key, value);
        entries.put(key, e);
        e.next = target;
        e.previous = target.previous;
        if (target.previous != null) target.previous.next = e;
        else head = e;
        target.previous = e;
        changes++;
        return null;
    }

    private void remove(Entry e) {
        if (e.previous != null) e.previous.next = e.next;
        else head = e.next;
        if (e.next != null) e.next.previous = e.previous;
        else last = e.previous;
        e.previous = null;
        e.next = null;
        changes++;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<>() {
            private final int expectedChanges = changes;
            private Entry current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Map.Entry<K, V> next() {
                if (changes != expectedChanges) throw new java.util.ConcurrentModificationException();
                if (current == null) throw new NoSuchElementException();
                Entry e = current;
                current = current.next;
                return e;
            }
        };
    }

    public interface MergeFunction<Key, Value> {
        Value apply(Key key, Value left, Value right);
    }

    private class Entry implements Map.Entry<K, V> {
        final K key;
        V value;
        Entry previous;
        Entry next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode() {
            return strategy.hashCode(key) ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (obj instanceof Map.Entry<?, ?> other) {
                return strategy.equals(key, (K) other.getKey()) && java.util.Objects.equals(value, other.getValue());
            }
            return false;
        }
    }
}
