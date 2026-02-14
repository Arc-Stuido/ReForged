package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;

/**
 * NeoForge RenderTypeGroup — mirrors the NeoForge reference implementation.
 */
public record RenderTypeGroup(RenderType block, RenderType entity, RenderType entityFabulous) {
    public RenderTypeGroup {
        if ((block == null) != (entity == null) || (block == null) != (entityFabulous == null))
            throw new IllegalArgumentException("The render types in a group must either be all null, or all non-null.");
    }

    public RenderTypeGroup(RenderType block, RenderType entity) {
        this(block, entity, entity);
    }

    public static final RenderTypeGroup EMPTY = new RenderTypeGroup(null, null, null);

    public boolean isEmpty() {
        return block == null;
    }

    /**
     * Convert a Forge RenderTypeGroup to a NeoForge RenderTypeGroup.
     */
    public static RenderTypeGroup fromForge(net.minecraftforge.client.RenderTypeGroup forgeGroup) {
        if (forgeGroup.isEmpty()) return EMPTY;
        return new RenderTypeGroup(forgeGroup.block(), forgeGroup.entity(), forgeGroup.entityFabulous());
    }
}
