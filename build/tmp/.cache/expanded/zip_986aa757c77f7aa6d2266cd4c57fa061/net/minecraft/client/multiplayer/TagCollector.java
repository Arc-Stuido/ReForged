package net.minecraft.client.multiplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TagCollector {
    private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap<>();

    public void append(ResourceKey<? extends Registry<?>> p_327817_, TagNetworkSerialization.NetworkPayload p_332646_) {
        this.tags.put(p_327817_, p_332646_);
    }

    private static void refreshBuiltInTagDependentData() {
        AbstractFurnaceBlockEntity.invalidateCache();
        Blocks.rebuildCache();
    }

    private void applyTags(RegistryAccess p_327703_, Predicate<ResourceKey<? extends Registry<?>>> p_334924_) {
        this.tags.forEach((p_335891_, p_332296_) -> {
            if (p_334924_.test((ResourceKey<? extends Registry<?>>)p_335891_)) {
                p_332296_.applyToRegistry(p_327703_.registryOrThrow((ResourceKey<? extends Registry<?>>)p_335891_));
            }
        });
    }

    public void updateTags(RegistryAccess p_333230_, boolean p_331570_) {
        if (p_331570_) {
            this.applyTags(p_333230_, RegistrySynchronization.NETWORKABLE_REGISTRIES::contains);
        } else {
            p_333230_.registries()
                .filter(p_331412_ -> !RegistrySynchronization.NETWORKABLE_REGISTRIES.contains(p_331412_.key()))
                .forEach(p_328076_ -> p_328076_.value().resetTags());
            this.applyTags(p_333230_, p_328746_ -> true);
            refreshBuiltInTagDependentData();
        }
    }
}