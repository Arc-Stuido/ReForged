package net.neoforged.neoforge.common.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.ExistingFileHelper.ResourceType;

/**
 * Stub: Dataprovider for using a Codec to generate jsons.
 * Path names for jsons are derived from the given registry folder and each entry's namespaced id.
 *
 * @param <T> the type of thing being generated.
 */
public abstract class JsonCodecProvider<T> implements DataProvider {
    protected final ResourceType resourceType;
    protected final PackOutput.PathProvider pathProvider;
    protected final ExistingFileHelper existingFileHelper;
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final String modid;
    protected final String directory;
    protected final Codec<T> codec;
    protected final Map<ResourceLocation, WithConditions<T>> conditions = Maps.newHashMap();

    /**
     * @param output    {@linkplain PackOutput} provided by the DataGenerator.
     * @param target    PackOutput.Target (DATA_PACK or RESOURCE_PACK).
     * @param directory String representing the directory to generate jsons in.
     * @param packType  PackType specifying whether to generate entries in assets or data.
     * @param codec     Codec to encode values to jsons with.
     * @param lookupProvider Future providing registry access.
     * @param modId     The mod id.
     * @param existingFileHelper Helper to check/track existing files.
     */
    public JsonCodecProvider(PackOutput output,
            PackOutput.Target target,
            String directory,
            PackType packType,
            Codec<T> codec,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            String modId,
            ExistingFileHelper existingFileHelper) {
        this.resourceType = new ResourceType(packType, ".json", directory);
        this.pathProvider = output.createPathProvider(target, directory);
        this.existingFileHelper = existingFileHelper;
        this.modid = modId;
        this.directory = directory;
        this.codec = codec;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        gather();

        return lookupProvider.thenCompose(provider -> {
            final DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, provider);

            this.conditions.forEach((id, withConditions) -> {
                final Path path = this.pathProvider.json(id);
                futuresBuilder.add(CompletableFuture.supplyAsync(() -> {
                    return this.codec.encodeStart(dynamicOps, withConditions.carrier())
                            .getOrThrow(msg -> new RuntimeException("Failed to encode %s: %s".formatted(path, msg)));
                }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, path)));
            });

            return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
        });
    }

    /**
     * Subclasses override this to register their data entries via {@link #unconditional} or {@link #conditionally}.
     */
    protected abstract void gather();

    @Override
    public String getName() {
        return String.format("%s generator for %s", this.directory, this.modid);
    }

    /**
     * Register an unconditional entry.
     */
    public void unconditional(ResourceLocation id, T value) {
        process(id, new WithConditions<>(List.of(), value));
    }

    /**
     * Register a conditional entry via a builder consumer.
     */
    public void conditionally(ResourceLocation id, Consumer<WithConditions.Builder<T>> configurator) {
        final WithConditions.Builder<T> builder = new WithConditions.Builder<>();
        configurator.accept(builder);
        final WithConditions<T> withConditions = builder.build();
        process(id, withConditions);
    }

    private void process(ResourceLocation id, WithConditions<T> withConditions) {
        this.existingFileHelper.trackGenerated(id, this.resourceType);
        this.conditions.put(id, withConditions);
    }
}
