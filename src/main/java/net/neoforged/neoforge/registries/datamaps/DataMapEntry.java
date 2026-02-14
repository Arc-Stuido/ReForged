package net.neoforged.neoforge.registries.datamaps;

/**
 * Stub: NeoForge's DataMapEntry record.
 */
public record DataMapEntry<T>(T value, boolean replace) {
    public DataMapEntry(T value) {
        this(value, false);
    }
}
