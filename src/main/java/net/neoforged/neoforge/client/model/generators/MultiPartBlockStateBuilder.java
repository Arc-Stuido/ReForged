package net.neoforged.neoforge.client.model.generators;

import net.minecraft.world.level.block.Block;

/**
 * Stub: NeoForge MultiPartBlockStateBuilder — builder for multipart block states.
 */
public class MultiPartBlockStateBuilder implements IGeneratedBlockState {
    private final Block owner;

    public MultiPartBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Block getOwner() { return owner; }

    public static class PartBuilder {
        public static class ConditionGroup {
        }
    }
}
