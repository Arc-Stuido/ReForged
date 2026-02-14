package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mixin to alias NeoForge convention tags ({@code c:*}) to Forge common tags ({@code forge:*})
 * and vice versa. This ensures items/blocks tagged with {@code #c:ingots} are also visible
 * under {@code #forge:ingots}, allowing NeoForge mods to query convention tags seamlessly.
 *
 * <p>Intercepts {@link TagLoader#build(Map)} which receives the raw loaded tag data
 * before building the final tag collections. We merge entries bidirectionally
 * between the two namespaces.</p>
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();

    /**
     * Modify the tag map parameter passed to build() to include cross-namespace aliases.
     * For every tag in "c:" namespace, copy its entries into the corresponding "forge:" tag
     * and vice versa.
     */
    @ModifyVariable(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"), argsOnly = true, remap = false)
    private Map<ResourceLocation, List<TagLoader.EntryWithSource>> reforged$aliasConventionTags(
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {

        // Work on a mutable copy
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> mutable = new HashMap<>(tagMap);
        int aliased = 0;

        // Collect all keys first to avoid ConcurrentModificationException
        List<Map.Entry<ResourceLocation, List<TagLoader.EntryWithSource>>> entries =
                new ArrayList<>(mutable.entrySet());

        for (Map.Entry<ResourceLocation, List<TagLoader.EntryWithSource>> entry : entries) {
            ResourceLocation key = entry.getKey();
            List<TagLoader.EntryWithSource> sources = entry.getValue();
            if (sources == null || sources.isEmpty()) continue;

            ResourceLocation alias = null;

            if ("c".equals(key.getNamespace())) {
                // c:foo → forge:foo
                alias = ResourceLocation.fromNamespaceAndPath("forge", key.getPath());
            } else if ("forge".equals(key.getNamespace())) {
                // forge:foo → c:foo
                alias = ResourceLocation.fromNamespaceAndPath("c", key.getPath());
            }

            if (alias != null) {
                List<TagLoader.EntryWithSource> existing = mutable.get(alias);
                if (existing == null) {
                    // Alias doesn't exist yet — create it with all entries from the source
                    mutable.put(alias, new ArrayList<>(sources));
                    aliased++;
                } else {
                    // Alias exists — merge entries that aren't already present
                    List<TagLoader.EntryWithSource> merged = new ArrayList<>(existing);
                    for (TagLoader.EntryWithSource src : sources) {
                        if (!merged.contains(src)) {
                            merged.add(src);
                        }
                    }
                    mutable.put(alias, merged);
                    aliased++;
                }
            }
        }

        if (aliased > 0) {
            REFORGED_LOGGER.debug("[ReForged] Aliased {} convention tags between c: and forge: namespaces", aliased);
        }

        return mutable;
    }
}
