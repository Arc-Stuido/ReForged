package net.neoforged.neoforge.client.model.generators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Stub: NeoForge BlockStateProvider — abstract data provider for block states and models.
 */
public abstract class BlockStateProvider implements DataProvider {

    protected final Map<Block, IGeneratedBlockState> registeredBlocks = new LinkedHashMap<>();
    private final PackOutput output;
    private final String modid;
    private final BlockModelProvider blockModels;
    private final ItemModelProvider itemModels;

    public BlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        this.output = output;
        this.modid = modid;
        this.blockModels = new BlockModelProvider(output, modid, exFileHelper) {
            @Override
            protected void registerModels() {}
        };
        this.itemModels = new ItemModelProvider(output, modid, exFileHelper) {
            @Override
            protected void registerModels() {}
        };
    }

    protected abstract void registerStatesAndModels();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        registeredBlocks.clear();
        registerStatesAndModels();
        return CompletableFuture.completedFuture(null);
    }

    public VariantBlockStateBuilder getVariantBuilder(Block b) {
        VariantBlockStateBuilder ret = new VariantBlockStateBuilder(b);
        registeredBlocks.put(b, ret);
        return ret;
    }

    public MultiPartBlockStateBuilder getMultipartBuilder(Block b) {
        MultiPartBlockStateBuilder ret = new MultiPartBlockStateBuilder(b);
        registeredBlocks.put(b, ret);
        return ret;
    }

    public BlockModelProvider models() { return blockModels; }
    public ItemModelProvider itemModels() { return itemModels; }

    public ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(modid, name);
    }

    public ResourceLocation mcLoc(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    public ResourceLocation blockTexture(Block block) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + name.getPath());
    }

    public ModelFile cubeAll(Block block) {
        return models().cubeAll(BuiltInRegistries.BLOCK.getKey(block).getPath(), blockTexture(block));
    }

    public void simpleBlock(Block block) {
        simpleBlock(block, cubeAll(block));
    }

    public void simpleBlock(Block block, Function<ModelFile, ConfiguredModel[]> expander) {
        simpleBlock(block, expander.apply(cubeAll(block)));
    }

    public void simpleBlock(Block block, ModelFile model) {
        simpleBlock(block, new ConfiguredModel(model));
    }

    public void simpleBlockItem(Block block, ModelFile model) {
        itemModels().getBuilder(BuiltInRegistries.BLOCK.getKey(block).getPath()).parent(model);
    }

    public void simpleBlockWithItem(Block block, ModelFile model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    public void simpleBlock(Block block, ConfiguredModel... models) {
        getVariantBuilder(block);
    }

    @Override
    public String getName() {
        return "Block States: " + modid;
    }
}
