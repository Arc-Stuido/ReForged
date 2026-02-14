package net.neoforged.neoforge.client.model.generators;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Stub: NeoForge ItemModelBuilder — concrete ModelBuilder for item models with override support.
 */
public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {
    protected List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    public OverrideBuilder override() {
        OverrideBuilder ret = new OverrideBuilder();
        overrides.add(ret);
        return ret;
    }

    public OverrideBuilder override(int index) {
        OverrideBuilder ret = new OverrideBuilder();
        overrides.add(index, ret);
        return ret;
    }

    public class OverrideBuilder {
        private ModelFile model;

        public OverrideBuilder model(ModelFile model) {
            this.model = model;
            return this;
        }

        public OverrideBuilder predicate(ResourceLocation key, float value) {
            return this;
        }

        public ItemModelBuilder end() {
            return ItemModelBuilder.this;
        }
    }
}
