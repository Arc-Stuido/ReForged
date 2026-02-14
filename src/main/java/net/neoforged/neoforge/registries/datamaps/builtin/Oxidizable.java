package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Oxidizable(Block nextOxidationStage) {
    public static final Codec<Oxidizable> CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Oxidizable::new, Oxidizable::nextOxidationStage);
}
