package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Waxable(Block waxed) {
    public static final Codec<Waxable> CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Waxable::new, Waxable::waxed);
}
