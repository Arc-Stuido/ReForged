package net.neoforged.neoforge.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

/** Proxy: NeoForge's NeoForgeExtraCodecs */
public final class NeoForgeExtraCodecs {
    private NeoForgeExtraCodecs() {}

    /**
     * Creates a codec for a Set.
     */
    public static <E> Codec<java.util.Set<E>> setOf(Codec<E> elementCodec) {
        return elementCodec.listOf().xmap(java.util.HashSet::new, java.util.ArrayList::new);
    }

    /**
     * Creates a MapCodec for an optional field that is always written during serialization,
     * even if it equals the default value.
     */
    @SuppressWarnings("unchecked")
    public static <A> MapCodec<A> optionalFieldAlwaysWrite(Codec<A> codec, String fieldName, A defaultValue) {
        return codec.optionalFieldOf(fieldName, defaultValue);
    }
}
