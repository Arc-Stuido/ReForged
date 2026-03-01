package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RegistryDataCollector {
    @Nullable
    private RegistryDataCollector.ContentsCollector contentsCollector;
    @Nullable
    private TagCollector tagCollector;

    public void appendContents(ResourceKey<? extends Registry<?>> p_331647_, List<RegistrySynchronization.PackedRegistryEntry> p_327881_) {
        if (this.contentsCollector == null) {
            this.contentsCollector = new RegistryDataCollector.ContentsCollector();
        }

        this.contentsCollector.append(p_331647_, p_327881_);
    }

    public void appendTags(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> p_329188_) {
        if (this.tagCollector == null) {
            this.tagCollector = new TagCollector();
        }

        p_329188_.forEach(this.tagCollector::append);
    }

    public RegistryAccess.Frozen collectGameRegistries(ResourceProvider p_333941_, RegistryAccess p_334865_, boolean p_328462_) {
        LayeredRegistryAccess<ClientRegistryLayer> layeredregistryaccess = ClientRegistryLayer.createRegistryAccess();
        RegistryAccess registryaccess;
        if (this.contentsCollector != null) {
            RegistryAccess.Frozen registryaccess$frozen = layeredregistryaccess.getAccessForLoading(ClientRegistryLayer.REMOTE);
            RegistryAccess.Frozen registryaccess$frozen1 = this.contentsCollector.loadRegistries(p_333941_, registryaccess$frozen).freeze();
            registryaccess = layeredregistryaccess.replaceFrom(ClientRegistryLayer.REMOTE, registryaccess$frozen1).compositeAccess();
        } else {
            registryaccess = p_334865_;
        }

        if (this.tagCollector != null) {
            this.tagCollector.updateTags(registryaccess, p_328462_);
        }

        return registryaccess.freeze();
    }

    @OnlyIn(Dist.CLIENT)
    static class ContentsCollector {
        private final Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap<>();

        public void append(ResourceKey<? extends Registry<?>> p_331127_, List<RegistrySynchronization.PackedRegistryEntry> p_331340_) {
            this.elements.computeIfAbsent(p_331127_, p_332834_ -> new ArrayList<>()).addAll(p_331340_);
        }

        public RegistryAccess loadRegistries(ResourceProvider p_331350_, RegistryAccess p_331174_) {
            return RegistryDataLoader.load(this.elements, p_331350_, p_331174_, RegistryDataLoader.SYNCHRONIZED_REGISTRIES);
        }
    }
}