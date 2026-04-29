package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
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
     * Inject at the HEAD of build() to add cross-namespace aliases directly into the
     * mutable tag map before it is processed.
     *
     * For every tag in "c:" namespace, copy its entries into the corresponding "forge:" tag
     * and vice versa.
     */
    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"), remap = false)
    private void reforged$aliasConventionTags(
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
            CallbackInfoReturnable<Map<ResourceLocation, ?>> cir) {

        REFORGED_LOGGER.debug("[ReForged] TagLoaderMixin.build() intercepted, processing {} tags", tagMap.size());

        int aliased = 0;

        // Collect entries to add (avoid ConcurrentModificationException)
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> additions = new java.util.HashMap<>();

        for (Map.Entry<ResourceLocation, List<TagLoader.EntryWithSource>> entry : tagMap.entrySet()) {
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
                List<TagLoader.EntryWithSource> existing = tagMap.get(alias);
                if (existing == null) {
                    existing = additions.get(alias);
                }

                if (existing == null) {
                    // Alias doesn't exist yet — create it with all entries from the source
                    additions.put(alias, new ArrayList<>(sources));
                    aliased++;
                } else {
                    // Alias exists — merge entries that aren't already present
                    List<TagLoader.EntryWithSource> merged = new ArrayList<>(existing);
                    for (TagLoader.EntryWithSource src : sources) {
                        if (!merged.contains(src)) {
                            merged.add(src);
                        }
                    }
                    additions.put(alias, merged);
                    aliased++;
                }
            }
        }

        // Apply all additions into the original map (it should be mutable at this point)
        if (!additions.isEmpty()) {
            try {
                tagMap.putAll(additions);
                REFORGED_LOGGER.debug("[ReForged] Aliased {} convention tags between c: and forge: namespaces", aliased);
            } catch (UnsupportedOperationException e) {
                // Map is immutable — can't modify in place, this shouldn't happen with TagLoader
                REFORGED_LOGGER.error("[ReForged] Tag map is immutable, cannot add aliases! Map type: {}", tagMap.getClass().getName());
            }
        }
    }
}
