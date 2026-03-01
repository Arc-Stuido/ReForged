package net.minecraft.client.resources.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BlockStateModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final int SINGLETON_MODEL_GROUP = -1;
    private static final int INVISIBLE_MODEL_GROUP = 0;
    public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
        .add(BooleanProperty.create("map"))
        .create(Block::defaultBlockState, BlockState::new);
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = Map.of(
        ResourceLocation.withDefaultNamespace("item_frame"), ITEM_FRAME_FAKE_DEFINITION, ResourceLocation.withDefaultNamespace("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION
    );
    private final Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources;
    private final ProfilerFiller profiler;
    private final BlockColors blockColors;
    private final BiConsumer<ModelResourceLocation, UnbakedModel> discoveredModelOutput;
    private int nextModelGroup = 1;
    private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), p_342055_ -> p_342055_.defaultReturnValue(-1));
    private final BlockStateModelLoader.LoadedModel missingModel;
    private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();

    public BlockStateModelLoader(
        Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> p_343575_,
        ProfilerFiller p_343300_,
        UnbakedModel p_344187_,
        BlockColors p_342820_,
        BiConsumer<ModelResourceLocation, UnbakedModel> p_344332_
    ) {
        this.blockStateResources = p_343575_;
        this.profiler = p_343300_;
        this.blockColors = p_342820_;
        this.discoveredModelOutput = p_344332_;
        BlockStateModelLoader.ModelGroupKey blockstatemodelloader$modelgroupkey = new BlockStateModelLoader.ModelGroupKey(List.of(p_344187_), List.of());
        this.missingModel = new BlockStateModelLoader.LoadedModel(p_344187_, () -> blockstatemodelloader$modelgroupkey);
    }

    public void loadAllBlockStates() {
        this.profiler.push("static_definitions");
        STATIC_DEFINITIONS.forEach(this::loadBlockStateDefinitions);
        this.profiler.popPush("blocks");

        for (Block block : BuiltInRegistries.BLOCK) {
            this.loadBlockStateDefinitions(block.builtInRegistryHolder().key().location(), block.getStateDefinition());
        }

        this.profiler.pop();
    }

    private void loadBlockStateDefinitions(ResourceLocation p_343375_, StateDefinition<Block, BlockState> p_342234_) {
        this.context.setDefinition(p_342234_);
        List<Property<?>> list = List.copyOf(this.blockColors.getColoringProperties(p_342234_.getOwner()));
        List<BlockState> list1 = p_342234_.getPossibleStates();
        Map<ModelResourceLocation, BlockState> map = new HashMap<>();
        list1.forEach(p_345251_ -> map.put(BlockModelShaper.stateToModelLocation(p_343375_, p_345251_), p_345251_));
        Map<BlockState, BlockStateModelLoader.LoadedModel> map1 = new HashMap<>();
        ResourceLocation resourcelocation = BLOCKSTATE_LISTER.idToFile(p_343375_);

        try {
            for (BlockStateModelLoader.LoadedJson blockstatemodelloader$loadedjson : this.blockStateResources.getOrDefault(resourcelocation, List.of())) {
                BlockModelDefinition blockmodeldefinition = blockstatemodelloader$loadedjson.parse(p_343375_, this.context);
                Map<BlockState, BlockStateModelLoader.LoadedModel> map2 = new IdentityHashMap<>();
                MultiPart multipart;
                if (blockmodeldefinition.isMultiPart()) {
                    multipart = blockmodeldefinition.getMultiPart();
                    list1.forEach(
                        p_345048_ -> map2.put(
                                p_345048_,
                                new BlockStateModelLoader.LoadedModel(
                                    multipart, () -> BlockStateModelLoader.ModelGroupKey.create(p_345048_, multipart, list)
                                )
                            )
                    );
                } else {
                    multipart = null;
                }

                blockmodeldefinition.getVariants()
                    .forEach(
                        (p_345103_, p_345100_) -> {
                            try {
                                list1.stream()
                                    .filter(predicate(p_342234_, p_345103_))
                                    .forEach(
                                        p_344771_ -> {
                                            BlockStateModelLoader.LoadedModel blockstatemodelloader$loadedmodel = map2.put(
                                                p_344771_,
                                                new BlockStateModelLoader.LoadedModel(
                                                    p_345100_, () -> BlockStateModelLoader.ModelGroupKey.create(p_344771_, p_345100_, list)
                                                )
                                            );
                                            if (blockstatemodelloader$loadedmodel != null && blockstatemodelloader$loadedmodel.model != multipart) {
                                                map2.put(p_344771_, this.missingModel);
                                                throw new RuntimeException(
                                                    "Overlapping definition with: "
                                                        + blockmodeldefinition.getVariants()
                                                            .entrySet()
                                                            .stream()
                                                            .filter(p_343452_ -> p_343452_.getValue() == blockstatemodelloader$loadedmodel.model)
                                                            .findFirst()
                                                            .get()
                                                            .getKey()
                                                );
                                            }
                                        }
                                    );
                            } catch (Exception exception1) {
                                LOGGER.warn(
                                    "Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}",
                                    resourcelocation,
                                    blockstatemodelloader$loadedjson.source,
                                    p_345103_,
                                    exception1.getMessage()
                                );
                            }
                        }
                    );
                map1.putAll(map2);
            }
        } catch (BlockStateModelLoader.BlockStateDefinitionException blockstatemodelloader$blockstatedefinitionexception) {
            LOGGER.warn("{}", blockstatemodelloader$blockstatedefinitionexception.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Exception loading blockstate definition: '{}'", resourcelocation, exception);
        } finally {
            Map<BlockStateModelLoader.ModelGroupKey, Set<BlockState>> map3 = new HashMap<>();
            map.forEach((p_342418_, p_345470_) -> {
                BlockStateModelLoader.LoadedModel blockstatemodelloader$loadedmodel = map1.get(p_345470_);
                if (blockstatemodelloader$loadedmodel == null) {
                    LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourcelocation, p_342418_);
                    blockstatemodelloader$loadedmodel = this.missingModel;
                }

                this.discoveredModelOutput.accept(p_342418_, blockstatemodelloader$loadedmodel.model);

                try {
                    BlockStateModelLoader.ModelGroupKey blockstatemodelloader$modelgroupkey = blockstatemodelloader$loadedmodel.key().get();
                    map3.computeIfAbsent(blockstatemodelloader$modelgroupkey, p_342745_ -> Sets.newIdentityHashSet()).add(p_345470_);
                } catch (Exception exception1) {
                    LOGGER.warn("Exception evaluating model definition: '{}'", p_342418_, exception1);
                }
            });
            map3.forEach((p_344557_, p_342237_) -> {
                Iterator<BlockState> iterator = p_342237_.iterator();

                while (iterator.hasNext()) {
                    BlockState blockstate = iterator.next();
                    if (blockstate.getRenderShape() != RenderShape.MODEL) {
                        iterator.remove();
                        this.modelGroups.put(blockstate, 0);
                    }
                }

                if (p_342237_.size() > 1) {
                    this.registerModelGroup(p_342237_);
                }
            });
        }
    }

    private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> p_344966_, String p_342983_) {
        Map<Property<?>, Comparable<?>> map = new HashMap<>();

        for (String s : COMMA_SPLITTER.split(p_342983_)) {
            Iterator<String> iterator = EQUAL_SPLITTER.split(s).iterator();
            if (iterator.hasNext()) {
                String s1 = iterator.next();
                Property<?> property = p_344966_.getProperty(s1);
                if (property != null && iterator.hasNext()) {
                    String s2 = iterator.next();
                    Comparable<?> comparable = getValueHelper((Property)property, s2);
                    if (comparable == null) {
                        throw new RuntimeException("Unknown value: '" + s2 + "' for blockstate property: '" + s1 + "' " + property.getPossibleValues());
                    }

                    map.put(property, comparable);
                } else if (!s1.isEmpty()) {
                    throw new RuntimeException("Unknown blockstate property: '" + s1 + "'");
                }
            }
        }

        Block block = p_344966_.getOwner();
        return p_343589_ -> {
            if (p_343589_ != null && p_343589_.is(block)) {
                for (Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                    if (!Objects.equals(p_343589_.getValue(entry.getKey()), entry.getValue())) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> p_343450_, String p_342834_) {
        return p_343450_.getValue(p_342834_).orElse(null);
    }

    private void registerModelGroup(Iterable<BlockState> p_342753_) {
        int i = this.nextModelGroup++;
        p_342753_.forEach(p_345338_ -> this.modelGroups.put(p_345338_, i));
    }

    public Object2IntMap<BlockState> getModelGroups() {
        return this.modelGroups;
    }

    @OnlyIn(Dist.CLIENT)
    static class BlockStateDefinitionException extends RuntimeException {
        public BlockStateDefinitionException(String p_345210_) {
            super(p_345210_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record LoadedJson(String source, JsonElement data) {
        BlockModelDefinition parse(ResourceLocation p_343205_, BlockModelDefinition.Context p_344967_) {
            try {
                return BlockModelDefinition.fromJsonElement(p_344967_, this.data);
            } catch (Exception exception) {
                throw new BlockStateModelLoader.BlockStateDefinitionException(
                    String.format(
                        Locale.ROOT,
                        "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s",
                        p_343205_,
                        this.source,
                        exception.getMessage()
                    )
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record LoadedModel(UnbakedModel model, Supplier<BlockStateModelLoader.ModelGroupKey> key) {
    }

    @OnlyIn(Dist.CLIENT)
    static record ModelGroupKey(List<UnbakedModel> models, List<Object> coloringValues) {
        public static BlockStateModelLoader.ModelGroupKey create(BlockState p_344115_, MultiPart p_344686_, Collection<Property<?>> p_342621_) {
            StateDefinition<Block, BlockState> statedefinition = p_344115_.getBlock().getStateDefinition();
            List<UnbakedModel> list = p_344686_.getSelectors()
                .stream()
                .filter(p_345001_ -> p_345001_.getPredicate(statedefinition).test(p_344115_))
                .map(Selector::getVariant)
                .collect(Collectors.toUnmodifiableList());
            List<Object> list1 = getColoringValues(p_344115_, p_342621_);
            return new BlockStateModelLoader.ModelGroupKey(list, list1);
        }

        public static BlockStateModelLoader.ModelGroupKey create(BlockState p_342405_, UnbakedModel p_342829_, Collection<Property<?>> p_343727_) {
            List<Object> list = getColoringValues(p_342405_, p_343727_);
            return new BlockStateModelLoader.ModelGroupKey(List.of(p_342829_), list);
        }

        private static List<Object> getColoringValues(BlockState p_344919_, Collection<Property<?>> p_343393_) {
            return p_343393_.stream().map(p_344919_::getValue).collect(Collectors.toUnmodifiableList());
        }
    }
}