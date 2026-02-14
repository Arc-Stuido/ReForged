package net.neoforged.neoforge.attachment;

import java.util.function.Function;
import java.util.function.Supplier;

/** Proxy: NeoForge's AttachmentType for entity data attachments */
public class AttachmentType<T> {
    private final Supplier<T> defaultValue;

    private AttachmentType(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Supplier<T> defaultValueSupplier() { return defaultValue; }

    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(defaultValue);
    }

    /** NeoForge: serializable(Supplier) — creates a builder for INBTSerializable attachment types */
    @SuppressWarnings("unchecked")
    public static <S, T> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return new Builder<>(defaultValueSupplier);
    }

    /** NeoForge: serializable(Function) — creates a builder capturing the holder reference */
    @SuppressWarnings("unchecked")
    public static <S, T> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(() -> defaultValueConstructor.apply(null));
    }

    public static class Builder<T> {
        private final Supplier<T> defaultValue;

        public Builder(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Builder<T> serialize(Object serializer) { return this; }
        public Builder<T> copyOnDeath() { return this; }
        public Builder<T> copyHandler(Object handler) { return this; }

        public AttachmentType<T> build() {
            return new AttachmentType<>(defaultValue);
        }
    }
}
