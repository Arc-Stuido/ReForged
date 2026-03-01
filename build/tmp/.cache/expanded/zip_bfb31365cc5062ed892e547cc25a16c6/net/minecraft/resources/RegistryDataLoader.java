package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize(p_326161_ -> {
        Lifecycle lifecycle = p_326161_.map(KnownPack::isVanilla).map(p_326166_ -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistrationInfo(p_326161_, lifecycle);
    });
    public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = net.minecraftforge.registries.DataPackRegistriesHooks.grabWorldgenRegistries(
        new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE, Structure.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT_PROVIDER, EnchantmentProvider.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
        new RegistryDataLoader.RegistryData<>(Registries.LEVEL_STEM, LevelStem.CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES = net.minecraftforge.registries.DataPackRegistriesHooks.grabSynchronizedRegistries(
        new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.NETWORK_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC)
    );

    public static java.util.stream.Stream<RegistryDataLoader.RegistryData<?>> getWorldGenAndDimensionStream() {
        return java.util.stream.Stream.concat(RegistryDataLoader.WORLDGEN_REGISTRIES.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    public static RegistryAccess.Frozen load(ResourceManager p_252046_, RegistryAccess p_249916_, List<RegistryDataLoader.RegistryData<?>> p_250344_) {
        return load((p_326156_, p_326157_) -> p_326156_.loadFromResources(p_252046_, p_326157_), p_249916_, p_250344_);
    }

    public static RegistryAccess.Frozen load(
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> p_328212_,
        ResourceProvider p_335625_,
        RegistryAccess p_334195_,
        List<RegistryDataLoader.RegistryData<?>> p_329346_
    ) {
        return load((p_326153_, p_326154_) -> p_326153_.loadFromNetwork(p_328212_, p_335625_, p_326154_), p_334195_, p_329346_);
    }

    private static RegistryAccess.Frozen load(
        RegistryDataLoader.LoadingFunction p_332256_, RegistryAccess p_331736_, List<RegistryDataLoader.RegistryData<?>> p_333463_
    ) {
        Map<ResourceKey<?>, Exception> map = new HashMap<>();
        List<RegistryDataLoader.Loader<?>> list = p_333463_.stream()
            .map(p_326168_ -> p_326168_.create(Lifecycle.stable(), map))
            .collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoLookup registryops$registryinfolookup = createContext(p_331736_, list);
        list.forEach(p_326160_ -> p_332256_.apply((RegistryDataLoader.Loader<?>)p_326160_, registryops$registryinfolookup));
        list.forEach(p_341109_ -> {
            Registry<?> registry = p_341109_.registry();

            try {
                registry.freeze();
            } catch (Exception exception) {
                map.put(registry.key(), exception);
            }

            if (p_341109_.data.requiredNonEmpty && registry.size() == 0) {
                map.put(registry.key(), new IllegalStateException("Registry must be non-empty"));
            }
        });
        if (!map.isEmpty()) {
            logErrors(map);
            var buf = new StringBuilder("Failed to load registries, see debug.log for more details:");
            map.forEach((k,v) -> buf.append("\n\t").append(k.toString()).append(": ").append(v.getMessage()));
            throw new IllegalStateException(buf.toString());
        } else {
            return new RegistryAccess.ImmutableRegistryAccess(list.stream().map(RegistryDataLoader.Loader::registry).toList()).freeze();
        }
    }

    private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess p_256568_, List<RegistryDataLoader.Loader<?>> p_255821_) {
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        p_256568_.registries().forEach(p_255505_ -> map.put(p_255505_.key(), createInfoForContextRegistry(p_255505_.value())));
        p_255821_.forEach(p_341107_ -> map.put(p_341107_.registry.key(), createInfoForNewRegistry(p_341107_.registry)));
        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256014_) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(p_256014_));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> p_256020_) {
        return new RegistryOps.RegistryInfo<>(p_256020_.asLookup(), p_256020_.createRegistrationLookup(), p_256020_.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> p_256230_) {
        return new RegistryOps.RegistryInfo<>(p_256230_.asLookup(), p_256230_.asTagAddingLookup(), p_256230_.registryLifecycle());
    }

    private static void logErrors(Map<ResourceKey<?>, Exception> p_252325_) {
        StringWriter stringwriter = new StringWriter();
        PrintWriter printwriter = new PrintWriter(stringwriter);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> map = p_252325_.entrySet()
            .stream()
            .collect(
                Collectors.groupingBy(
                    p_249353_ -> p_249353_.getKey().registry(), Collectors.toMap(p_251444_ -> p_251444_.getKey().location(), Entry::getValue)
                )
            );
        map.entrySet().stream().sorted(Entry.comparingByKey()).forEach(p_249838_ -> {
            printwriter.printf("> Errors in registry %s:%n", p_249838_.getKey());
            p_249838_.getValue().entrySet().stream().sorted(Entry.comparingByKey()).forEach(p_250688_ -> {
                printwriter.printf(">> Errors in element %s:%n", p_250688_.getKey());
                p_250688_.getValue().printStackTrace(printwriter);
            });
        });
        printwriter.flush();
        LOGGER.error("Registry loading errors:\n{}", stringwriter);
    }

    private static <E> void loadElementFromResource(
        WritableRegistry<E> p_330991_,
        Decoder<Optional<E>> p_333909_,
        RegistryOps<JsonElement> p_332135_,
        ResourceKey<E> p_332850_,
        Resource p_335244_,
        RegistrationInfo p_332222_
    ) throws IOException {
        try (Reader reader = p_335244_.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);

            var result = p_333909_.decode(p_332135_, jsonelement);

            if (result.isError()) {
                LOGGER.error("Couldn't parse data file {}: {}", p_332850_, result.error().get().message());
                throw new IllegalStateException("Couldn't parse data file " + p_332850_ + ": " + result.error().get().message());
            }

            var mappedResult = result.result().map(a -> a.getFirst());

            if (mappedResult.get().isEmpty()) {
                LOGGER.debug("Skipping {} conditions not met", p_332850_);
                return;
            }

            DataResult<E> dataresult = result.map(p -> p.mapFirst(Optional::get)).map(p -> p.getFirst());

            E e = dataresult.getOrThrow();
            p_330991_.register(p_332850_, e, p_332222_);
        }
    }

    static <E> void loadContentsFromManager(
        ResourceManager p_335634_,
        RegistryOps.RegistryInfoLookup p_333035_,
        WritableRegistry<E> p_331358_,
        Decoder<E> p_329404_,
        Map<ResourceKey<?>, Exception> p_335074_
    ) {
        String s = Registries.elementsDirPath(p_331358_.key());
        FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
        RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, p_333035_);
        Decoder<Optional<E>> conditional = net.minecraftforge.common.crafting.conditions.ConditionCodec.wrap(p_329404_);

        for (Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(p_335634_).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceKey<E> resourcekey = ResourceKey.create(p_331358_.key(), filetoidconverter.fileToId(resourcelocation));
            Resource resource = entry.getValue();
            RegistrationInfo registrationinfo = REGISTRATION_INFO_CACHE.apply(resource.knownPackInfo());

            try {
                loadElementFromResource(p_331358_, conditional, registryops, resourcekey, resource, registrationinfo);
            } catch (Exception exception) {
                p_335074_.put(
                    resourcekey,
                    new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourcelocation, resource.sourcePackId()), exception)
                );
            }
        }
    }

    static <E> void loadContentsFromNetwork(
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> p_331925_,
        ResourceProvider p_332010_,
        RegistryOps.RegistryInfoLookup p_329253_,
        WritableRegistry<E> p_332518_,
        Decoder<E> p_328898_,
        Map<ResourceKey<?>, Exception> p_335768_
    ) {
        List<RegistrySynchronization.PackedRegistryEntry> list = p_331925_.get(p_332518_.key());
        if (list != null) {
            RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, p_329253_);
            RegistryOps<JsonElement> registryops1 = RegistryOps.create(JsonOps.INSTANCE, p_329253_)
                .withContext(net.minecraftforge.common.crafting.conditions.ICondition.IContext.KEY, net.minecraftforge.common.crafting.conditions.ICondition.IContext.TAGS_INVALID);
            Decoder<Optional<E>> conditional = net.minecraftforge.common.crafting.conditions.ConditionCodec.wrap(p_328898_);
            String s = Registries.elementsDirPath(p_332518_.key());
            FileToIdConverter filetoidconverter = FileToIdConverter.json(s);

            for (RegistrySynchronization.PackedRegistryEntry registrysynchronization$packedregistryentry : list) {
                ResourceKey<E> resourcekey = ResourceKey.create(p_332518_.key(), registrysynchronization$packedregistryentry.id());
                Optional<Tag> optional = registrysynchronization$packedregistryentry.data();
                if (optional.isPresent()) {
                    try {
                        DataResult<E> dataresult = p_328898_.parse(registryops, optional.get());
                        E e = dataresult.getOrThrow();
                        p_332518_.register(resourcekey, e, NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception) {
                        p_335768_.put(
                            resourcekey,
                            new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception)
                        );
                    }
                } else {
                    ResourceLocation resourcelocation = filetoidconverter.idToFile(registrysynchronization$packedregistryentry.id());

                    try {
                        Resource resource = p_332010_.getResourceOrThrow(resourcelocation);
                        loadElementFromResource(p_332518_, conditional, registryops1, resourcekey, resource, NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception1) {
                        p_335768_.put(resourcekey, new IllegalStateException("Failed to parse local data", exception1));
                    }
                }
            }
        }
    }

    static record Loader<T>(RegistryDataLoader.RegistryData<T> data, WritableRegistry<T> registry, Map<ResourceKey<?>, Exception> loadingErrors) {
        public void loadFromResources(ResourceManager p_328137_, RegistryOps.RegistryInfoLookup p_330371_) {
            RegistryDataLoader.loadContentsFromManager(p_328137_, p_330371_, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(
            Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> p_333047_,
            ResourceProvider p_333682_,
            RegistryOps.RegistryInfoLookup p_330665_
        ) {
            RegistryDataLoader.loadContentsFromNetwork(p_333047_, p_333682_, p_330665_, this.registry, this.data.elementCodec, this.loadingErrors);
        }
    }

    @FunctionalInterface
    interface LoadingFunction {
        void apply(RegistryDataLoader.Loader<?> p_332841_, RegistryOps.RegistryInfoLookup p_332366_);
    }

    public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
        RegistryData(ResourceKey<? extends Registry<T>> p_251360_, Codec<T> p_248976_) {
            this(p_251360_, p_248976_, false);
        }

        RegistryDataLoader.Loader<T> create(Lifecycle p_251662_, Map<ResourceKey<?>, Exception> p_251565_) {
            WritableRegistry<T> writableregistry = new MappedRegistry<>(this.key, p_251662_);
            return new RegistryDataLoader.Loader<>(this, writableregistry, p_251565_);
        }

        public void runWithArguments(BiConsumer<ResourceKey<? extends Registry<T>>, Codec<T>> p_310351_) {
            p_310351_.accept(this.key, this.elementCodec);
        }
    }
}
