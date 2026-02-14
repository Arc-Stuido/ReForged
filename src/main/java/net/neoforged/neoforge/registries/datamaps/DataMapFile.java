package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.WithConditions;

/**
 * Stub: NeoForge's DataMapFile record.
 */
public record DataMapFile<T, R>(
        boolean replace,
        Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values,
        List<Object> removals) {
}
