package org.xiyu.reforged.mixin;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

/**
 * Gracefully handle unbound {@link net.minecraft.core.Holder.Reference} in
 * structure sets from NeoForge mods (e.g. Twilight Forest).
 *
 * <p>The vanilla code iterates each structure entry's biomes via
 * {@code entry.structure().value().biomes()}, which throws
 * {@link IllegalStateException} when the holder is not yet bound.
 * This Mixin catches those errors per-entry so valid structures
 * within the same set are still processed.</p>
 */
@Mixin(ChunkGeneratorStructureState.class)
public class ChunkGeneratorStructureStateMixin {

    private static final Logger REFORGED_LOGGER = LogManager.getLogger("ReForged");

    /**
     * @reason Gracefully skip structures with unbound Holder references from NeoForge mods
     * @author ReForged
     */
    @Overwrite(remap = false)
    private static boolean hasBiomesForStructureSet(StructureSet structureSet, BiomeSource biomeSource) {
        try {
            return structureSet.structures().stream()
                .flatMap(entry -> {
                    try {
                        return entry.structure().value().biomes().stream();
                    } catch (Exception e) {
                        REFORGED_LOGGER.warn("[ReForged] Skipping unbound structure in set: {}", e.getMessage());
                        return Stream.empty();
                    }
                })
                .anyMatch(biomeSource.possibleBiomes()::contains);
        } catch (Exception e) {
            REFORGED_LOGGER.warn("[ReForged] Skipping structure set due to error: {}", e.getMessage());
            return false;
        }
    }
}
