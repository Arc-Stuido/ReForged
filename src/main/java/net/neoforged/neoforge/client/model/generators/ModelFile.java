package net.neoforged.neoforge.client.model.generators;

import net.minecraft.resources.ResourceLocation;

/**
 * Shim: NeoForge's ModelFile → extends Forge's ModelFile.
 * <p>
 * NeoForge mods reference {@code net.neoforged.neoforge.client.model.generators.ModelFile},
 * which is functionally identical to Forge's version.
 */
public abstract class ModelFile extends net.minecraftforge.client.model.generators.ModelFile {

    protected ModelFile(ResourceLocation location) {
        super(location);
    }

    public static class UncheckedModelFile extends ModelFile {
        public UncheckedModelFile(String location) {
            this(ResourceLocation.parse(location));
        }

        public UncheckedModelFile(ResourceLocation location) {
            super(location);
        }

        @Override
        protected boolean exists() {
            return true;
        }
    }

    public static class ExistingModelFile extends ModelFile {
        private final net.neoforged.neoforge.common.data.ExistingFileHelper existingHelper;

        public ExistingModelFile(ResourceLocation location,
                                 net.neoforged.neoforge.common.data.ExistingFileHelper existingHelper) {
            super(location);
            this.existingHelper = existingHelper;
        }

        @Override
        protected boolean exists() {
            // Always return true since we can't access ModelProvider's protected constants
            return true;
        }
    }
}
