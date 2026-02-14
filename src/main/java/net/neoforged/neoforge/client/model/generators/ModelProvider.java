package net.neoforged.neoforge.client.model.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Stub: NeoForge ModelProvider — abstract base for model data providers.
 */
public abstract class ModelProvider<T extends ModelBuilder<T>> implements DataProvider {
    public static final String BLOCK_FOLDER = "block";
    public static final String ITEM_FOLDER = "item";
    public static final ExistingFileHelper.ResourceType TEXTURE =
            new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
    protected static final ExistingFileHelper.ResourceType MODEL =
            new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");

    protected final PackOutput output;
    protected final String modid;
    protected final String folder;
    protected final Function<ResourceLocation, T> factory;
    public final Map<ResourceLocation, T> generatedModels = new HashMap<>();
    public final ExistingFileHelper existingFileHelper;

    public ModelProvider(PackOutput output, String modid, String folder,
                         Function<ResourceLocation, T> factory,
                         ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.modid = modid;
        this.folder = folder;
        this.factory = factory;
        this.existingFileHelper = existingFileHelper;
    }

    public ModelProvider(PackOutput output, String modid, String folder,
                         BiFunction<ResourceLocation, ExistingFileHelper, T> builderFromModId,
                         ExistingFileHelper existingFileHelper) {
        this(output, modid, folder, loc -> builderFromModId.apply(loc, existingFileHelper), existingFileHelper);
    }

    protected abstract void registerModels();

    public T getBuilder(String path) {
        ResourceLocation outputLoc;
        if (path.contains(":")) {
            outputLoc = ResourceLocation.parse(path);
        } else {
            outputLoc = ResourceLocation.fromNamespaceAndPath(modid, folder + "/" + path);
        }
        return generatedModels.computeIfAbsent(outputLoc, factory);
    }

    public ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(modid, name);
    }

    public ResourceLocation mcLoc(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    public T withExistingParent(String name, String parent) {
        return withExistingParent(name, ResourceLocation.parse(parent));
    }

    public T withExistingParent(String name, ResourceLocation parent) {
        return getBuilder(name).parent(new ModelFile.UncheckedModelFile(parent));
    }

    public T singleTexture(String name, ResourceLocation parent, ResourceLocation texture) {
        return singleTexture(name, parent, "layer0", texture);
    }

    public T singleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture) {
        return withExistingParent(name, parent).texture(textureKey, texture);
    }

    public T cubeAll(String name, ResourceLocation texture) {
        return singleTexture(name, mcLoc(BLOCK_FOLDER + "/cube_all"), "all", texture);
    }

    public T cubeColumn(String name, ResourceLocation side, ResourceLocation end) {
        return withExistingParent(name, mcLoc(BLOCK_FOLDER + "/cube_column"))
                .texture("side", side).texture("end", end);
    }

    public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
        return new ModelFile.ExistingModelFile(path, null);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        clear();
        registerModels();
        return CompletableFuture.completedFuture(null);
    }

    protected void clear() {
        generatedModels.clear();
    }

    @Override
    public String getName() {
        return "Models: " + modid;
    }
}
