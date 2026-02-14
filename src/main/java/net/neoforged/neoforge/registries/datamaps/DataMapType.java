package net.neoforged.neoforge.registries.datamaps;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's DataMapType — sealed class representing a data map type for a registry.
 */
public sealed class DataMapType<R, T> permits AdvancedDataMapType {
    private final ResourceKey<Registry<R>> registryKey;
    private final ResourceLocation id;
    private final Codec<T> codec;
    private final @Nullable Codec<T> networkCodec;
    private final boolean mandatorySync;

    DataMapType(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec,
                @Nullable Codec<T> networkCodec, boolean mandatorySync) {
        this.registryKey = registryKey;
        this.id = id;
        this.codec = codec;
        this.networkCodec = networkCodec;
        this.mandatorySync = mandatorySync;
    }

    public static <T, R> Builder<T, R> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<>(registry, id, codec);
    }

    public ResourceKey<Registry<R>> registryKey() { return registryKey; }
    public ResourceLocation id() { return id; }
    public Codec<T> codec() { return codec; }
    public @Nullable Codec<T> networkCodec() { return networkCodec; }
    public boolean mandatorySync() { return mandatorySync; }

    public static sealed class Builder<T, R> permits AdvancedDataMapType.Builder {
        protected final ResourceKey<Registry<R>> registryKey;
        protected final ResourceLocation id;
        protected final Codec<T> codec;
        protected @Nullable Codec<T> networkCodec;
        protected boolean mandatorySync;

        Builder(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec) {
            this.registryKey = registryKey;
            this.id = id;
            this.codec = codec;
        }

        public Builder<T, R> synced(Codec<T> networkCodec, boolean mandatory) {
            this.mandatorySync = mandatory;
            this.networkCodec = networkCodec;
            return this;
        }

        public DataMapType<R, T> build() {
            return new DataMapType<>(registryKey, id, codec, networkCodec, mandatorySync);
        }
    }
}
