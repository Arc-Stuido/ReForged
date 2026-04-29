package org.xiyu.reforged.shim.attachment;

import com.mojang.serialization.Codec;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * AttachmentType — Shim for NeoForge's data attachment type registry.
 *
 * <p>NeoForge mods create attachment types via:</p>
 * <pre>
 * public static final AttachmentType&lt;MyData&gt; MY_ATTACHMENT =
 *     AttachmentType.builder(() -&gt; new MyData()).serialize(MyData.CODEC).build();
 * </pre>
 *
 * @param <T> the attachment data type
 */
public final class AttachmentType<T> {

    private final String id;
    private final Function<IAttachmentHolder, T> defaultValueFactory;
    private final boolean serialize;
    @Nullable
    private final Codec<T> codec;

    private AttachmentType(Builder<T> builder) {
        this.id = builder.id != null ? builder.id : "unknown";
        this.defaultValueFactory = builder.defaultValueFactory;
        this.serialize = builder.serialize;
        this.codec = builder.codec;
    }

    public String id() {
        return id;
    }

    public Supplier<T> defaultValueSupplier() {
        return defaultValueFactory == null ? null : () -> defaultValueFactory.apply(null);
    }

    public T createDefaultValue(@Nullable IAttachmentHolder holder) {
        return defaultValueFactory == null ? null : defaultValueFactory.apply(holder);
    }

    public boolean shouldSerialize() {
        return serialize && codec != null;
    }

    /**
     * Returns the codec for serialization, or null if not serializable.
     */
    @Nullable
    public Codec<T> codec() {
        return codec;
    }

    // ─── Builder (matches NeoForge's API) ──────────────────────

    /**
     * Create a builder with a default value supplier.
     */
    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(holder -> defaultValue.get());
    }

    /**
     * Create a holder-aware builder.
     */
    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(defaultValueConstructor);
    }

    /**
     * NeoForge overload for INBTSerializable-style attachments.
     */
    public static <T> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return builder(defaultValueSupplier).serialize(new Object());
    }

    /**
     * NeoForge overload for holder-aware INBTSerializable-style attachments.
     */
    public static <T> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return builder(defaultValueConstructor).serialize(new Object());
    }

    /**
     * Create a builder without a default value.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>((Function<IAttachmentHolder, T>) null);
    }

    public static final class Builder<T> {
        private Function<IAttachmentHolder, T> defaultValueFactory;
        private String id;
        private boolean serialize = false;
        @Nullable
        private Codec<T> codec;

        Builder(Function<IAttachmentHolder, T> defaultValueFactory) {
            this.defaultValueFactory = defaultValueFactory;
        }

        /**
         * Set the serializer (NeoForge uses a Codec).
         */
        @SuppressWarnings("unchecked")
        public Builder<T> serialize(Object codecOrSerializer) {
            if (codecOrSerializer instanceof Codec<?> c) {
                this.codec = (Codec<T>) c;
            }
            this.serialize = true;
            return this;
        }

        /**
         * Set the serializer with a copy handler.
         */
        @SuppressWarnings("unchecked")
        public Builder<T> serialize(Object codec, Object copyHandler) {
            if (codec instanceof Codec<?> c) {
                this.codec = (Codec<T>) c;
            }
            this.serialize = true;
            return this;
        }

        /**
         * Copy on death flag.
         */
        public Builder<T> copyOnDeath() {
            return this;
        }

        /**
         * Copy handler.
         */
        public Builder<T> copyHandler(Object handler) {
            return this;
        }

        /**
         * Synchronization handler; networking is bridged elsewhere, so the builder
         * accepts the NeoForge call shape without changing local storage.
         */
        public Builder<T> sync(Object syncHandler) {
            return this;
        }

        public Builder<T> sync(BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer, Object streamCodec) {
            return this;
        }

        /**
         * Set a custom comparator.
         */
        public Builder<T> comparator(Object comparator) {
            return this;
        }

        /**
         * Build the attachment type.
         */
        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }

        // Allow setting id
        Builder<T> id(String id) {
            this.id = id;
            return this;
        }
    }
}
