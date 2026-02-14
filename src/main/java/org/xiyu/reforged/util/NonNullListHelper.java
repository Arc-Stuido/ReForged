package org.xiyu.reforged.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;

import java.util.Collection;
import java.util.function.Function;

/**
 * Helper class for NeoForge's NonNullList patches.
 * <p>
 * The Forge JS coremod injects static methods into {@link NonNullList}
 * that delegate to these methods. This avoids the complexity of generating
 * lambda bytecode in the coremod's ASM code.
 * </p>
 */
public final class NonNullListHelper {
    private NonNullListHelper() {}

    /**
     * Creates a Codec for NonNullList from an element codec.
     * Equivalent to NeoForge's {@code NonNullList.codecOf(Codec)}.
     */
    @SuppressWarnings("unchecked")
    public static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(
                list -> {
                    NonNullList<E> result = NonNullList.create();
                    result.addAll(list);
                    return result;
                },
                Function.identity()
        );
    }

    /**
     * Creates a NonNullList from a collection.
     * Equivalent to NeoForge's {@code NonNullList.copyOf(Collection)}.
     */
    public static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
        NonNullList<E> result = NonNullList.create();
        result.addAll(entries);
        return result;
    }
}
