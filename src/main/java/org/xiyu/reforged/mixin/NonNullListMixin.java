package org.xiyu.reforged.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Function;

/**
 * Mixin for {@link NonNullList}: adds NeoForge's {@code codecOf(Codec)} and
 * {@code copyOf(Collection)} static methods.
 * <p>
 * NeoForge patches NonNullList to add these utility methods. Since they're
 * called as {@code NonNullList.codecOf(...)}, they must exist on the class itself.
 * Mixin merges non-private, non-@Shadow methods into the target class.
 * </p>
 */
@Mixin(NonNullList.class)
public abstract class NonNullListMixin {

    /**
     * NeoForge patch: creates a Codec for NonNullList from an element codec.
     */
    @SuppressWarnings("unchecked")
    public static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(list -> {
            NonNullList<E> result = NonNullList.create();
            result.addAll(list);
            return result;
        }, Function.identity());
    }

    /**
     * NeoForge patch: creates a NonNullList from a collection.
     */
    public static <E> NonNullList<E> copyOf(java.util.Collection<? extends E> entries) {
        NonNullList<E> result = NonNullList.create();
        result.addAll(entries);
        return result;
    }
}
