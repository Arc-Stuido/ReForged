package net.neoforged.neoforge.attachment;

import java.util.Optional;
import java.util.function.Supplier;

/** Proxy: NeoForge's IAttachmentHolder — marks objects that can hold data attachments */
public interface IAttachmentHolder {
    default <T> T getData(AttachmentType<T> type) { return type.defaultValueSupplier().get(); }
    default <T> boolean hasData(AttachmentType<T> type) { return false; }
    default <T> T setData(AttachmentType<T> type, T value) { return value; }
    default <T> T removeData(AttachmentType<T> type) { return type.defaultValueSupplier().get(); }
    default boolean hasAttachments() { return false; }

    // Supplier overloads used by NeoForge mods
    default <T> T getData(Supplier<AttachmentType<T>> type) { return getData(type.get()); }
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) { return hasData(type.get()); }
    default <T> T setData(Supplier<AttachmentType<T>> type, T value) { return setData(type.get(), value); }
    default <T> T removeData(Supplier<AttachmentType<T>> type) { return removeData(type.get()); }
    default <T> Optional<T> getExistingData(AttachmentType<T> type) { return hasData(type) ? Optional.of(getData(type)) : Optional.empty(); }
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) { return getExistingData(type.get()); }
}
