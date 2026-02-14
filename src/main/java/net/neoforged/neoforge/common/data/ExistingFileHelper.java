package net.neoforged.neoforge.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

/**
 * Stub: NeoForge's ExistingFileHelper for data gen.
 */
public class ExistingFileHelper {
    public ExistingFileHelper() {}

    public interface IResourceType {
        PackType getPackType();
        String getSuffix();
        String getPrefix();
    }

    public static class ResourceType implements IResourceType {
        private final PackType packType;
        private final String suffix;
        private final String prefix;

        public ResourceType(PackType packType, String suffix, String prefix) {
            this.packType = packType;
            this.suffix = suffix;
            this.prefix = prefix;
        }

        @Override public PackType getPackType() { return packType; }
        @Override public String getSuffix() { return suffix; }
        @Override public String getPrefix() { return prefix; }
    }

    public boolean exists(ResourceLocation loc, IResourceType type) { return true; }
    public void trackGenerated(ResourceLocation loc, IResourceType type) {}
}
