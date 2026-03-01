package net.neoforged.neoforge.registries.holdersets;

import com.mojang.serialization.MapCodec;

/**
 * Stub: Holder set type descriptor.
 */
public record HolderSetType(MapCodec<? extends ICustomHolderSet<?>> codec) {
}
