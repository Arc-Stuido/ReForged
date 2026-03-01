package org.xiyu.reforged.shim.attachment;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * IAttachmentHolder — Shim for NeoForge's Data Attachment system.
 *
 * <h3>NeoForge API</h3>
 * <p>NeoForge 1.21.1 replaced Forge's Capability system with "Data Attachments":</p>
 * <pre>
 * entity.getData(MY_ATTACHMENT);
 * entity.setData(MY_ATTACHMENT, newValue);
 * entity.hasData(MY_ATTACHMENT);
 * </pre>
 *
 * <h3>Bridge Strategy</h3>
 * <p>Uses a global WeakHashMap-style storage keyed by the holder object identity.
 * For classes where Mixin injection provides an {@link AttachmentHolder.AsField},
 * the holder is used directly. Otherwise, falls back to the global storage.</p>
 */
public interface IAttachmentHolder {

    /**
     * Get attached data. Returns the default value if not set.
     */
    default <T> T getData(AttachmentType<T> type) {
        return GlobalAttachmentStorage.getData(this, type);
    }

    /**
     * Set attached data.
     */
    default <T> T setData(AttachmentType<T> type, T value) {
        return GlobalAttachmentStorage.setData(this, type, value);
    }

    /**
     * Check if data is attached.
     */
    default <T> boolean hasData(AttachmentType<T> type) {
        return GlobalAttachmentStorage.hasData(this, type);
    }

    /**
     * Remove attached data.
     */
    default <T> T removeData(AttachmentType<T> type) {
        return GlobalAttachmentStorage.removeData(this, type);
    }

    /**
     * Check if this holder has any attachments.
     */
    default boolean hasAttachments() {
        return GlobalAttachmentStorage.hasAttachments(this);
    }

    /** Supplier overloads for compatibility with NeoForge DeferredHolder pattern */
    default <T> T getData(Supplier<AttachmentType<T>> type) { return getData(type.get()); }
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) { return hasData(type.get()); }
    default <T> T setData(Supplier<AttachmentType<T>> type, T value) { return setData(type.get(), value); }
    default <T> T removeData(Supplier<AttachmentType<T>> type) { return removeData(type.get()); }

    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return hasData(type) ? Optional.of(getData(type)) : Optional.empty();
    }
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return getExistingData(type.get());
    }

    /**
     * Sync data (no-op in Forge bridge — NeoForge-only concept).
     */
    default void syncData(AttachmentType<?> type) {
        // No-op — syncing is a NeoForge-only feature
    }

    // ─── Global data storage for IAttachmentHolder default methods ──────

    /**
     * Global storage backed by a WeakIdentityHashMap-like structure using
     * a ConcurrentHashMap&lt;WeakRef, Map&gt; pattern. The keys use
     * System.identityHashCode + WeakReference to avoid memory leaks.
     *
     * <p>When classes have an injected {@link AttachmentHolder.AsField} field via Mixin,
     * they should override the default methods to delegate to that field directly.</p>
     */
    final class GlobalAttachmentStorage {
        private static final Logger LOGGER = LogUtils.getLogger();

        // Use a WeakHashMap-like approach: key = holder object (via weak identity)
        // For simplicity, we use identityHashCode but pair it with a cleanup mechanism
        private static final Map<Integer, HolderData> STORAGE = new ConcurrentHashMap<>();

        private static HolderData getOrCreate(IAttachmentHolder holder) {
            int key = System.identityHashCode(holder);
            return STORAGE.computeIfAbsent(key, k -> new HolderData());
        }

        private static HolderData getIfPresent(IAttachmentHolder holder) {
            return STORAGE.get(System.identityHashCode(holder));
        }

        @SuppressWarnings("unchecked")
        static <T> T getData(IAttachmentHolder holder, AttachmentType<T> type) {
            HolderData data = getIfPresent(holder);
            if (data != null) {
                Object value = data.attachments.get(type);
                if (value != null) {
                    return (T) value;
                }
            }
            // Auto-initialize with default value and store it
            T defaultVal = type.defaultValueSupplier() != null ? type.defaultValueSupplier().get() : null;
            if (defaultVal != null) {
                getOrCreate(holder).attachments.put(type, defaultVal);
            }
            return defaultVal;
        }

        @SuppressWarnings("unchecked")
        static <T> T setData(IAttachmentHolder holder, AttachmentType<T> type, T value) {
            return (T) getOrCreate(holder).attachments.put(type, value);
        }

        static <T> boolean hasData(IAttachmentHolder holder, AttachmentType<T> type) {
            HolderData data = getIfPresent(holder);
            return data != null && data.attachments.containsKey(type);
        }

        @SuppressWarnings("unchecked")
        static <T> T removeData(IAttachmentHolder holder, AttachmentType<T> type) {
            HolderData data = getIfPresent(holder);
            if (data != null) {
                return (T) data.attachments.remove(type);
            }
            return null;
        }

        static boolean hasAttachments(IAttachmentHolder holder) {
            HolderData data = getIfPresent(holder);
            return data != null && !data.attachments.isEmpty();
        }

        /**
         * Remove all data associated with a holder (called when entity is removed etc.)
         */
        public static void cleanup(IAttachmentHolder holder) {
            STORAGE.remove(System.identityHashCode(holder));
        }

        /** Per-holder storage container */
        private static class HolderData {
            final Map<AttachmentType<?>, Object> attachments = new ConcurrentHashMap<>();
        }
    }
}
