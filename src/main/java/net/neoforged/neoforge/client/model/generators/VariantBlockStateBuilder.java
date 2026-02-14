package net.neoforged.neoforge.client.model.generators;

import net.minecraft.world.level.block.Block;

/**
 * Stub: NeoForge VariantBlockStateBuilder — builder for variant block states.
 */
public class VariantBlockStateBuilder implements IGeneratedBlockState {
    private final Block owner;

    public VariantBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Block getOwner() { return owner; }

    public static class PartialBlockstate {
    }
}
