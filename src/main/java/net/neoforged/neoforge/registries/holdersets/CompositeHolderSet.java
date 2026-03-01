package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stub: Base class for composite holder sets.
 */
public abstract class CompositeHolderSet<T> extends HolderSet.ListBacked<T> {
    @Override
    protected List<Holder<T>> contents() {
        return Collections.emptyList();
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return contents().contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }
}
