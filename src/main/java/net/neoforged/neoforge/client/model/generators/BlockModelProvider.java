package net.neoforged.neoforge.client.model.generators;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Stub: NeoForge BlockModelProvider — data provider for block models.
 */
public abstract class BlockModelProvider extends ModelProvider<BlockModelBuilder> {
    public BlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, BLOCK_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Block Models: " + modid;
    }
}
