package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private Map<ResourceLocation, AdvancementHolder> advancements = Map.of();
    private AdvancementTree tree = new AdvancementTree();
    private final HolderLookup.Provider registries;
    private final net.minecraftforge.common.crafting.conditions.ICondition.IContext context; //Forge: add context

    /** @deprecated Forge: use {@linkplain ServerAdvancementManager#ServerAdvancementManager(LootDataManager, net.minecraftforge.common.crafting.conditions.ICondition.IContext) constructor with context}. */
    @Deprecated
    public ServerAdvancementManager(HolderLookup.Provider p_336198_) {
        this(p_336198_, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
    }

    public ServerAdvancementManager(HolderLookup.Provider p_336198_, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
        super(GSON, Registries.elementsDirPath(Registries.ADVANCEMENT));
        this.registries = p_336198_;
        this.context = context;
    }

    protected void apply(Map<ResourceLocation, JsonElement> p_136034_, ResourceManager p_136035_, ProfilerFiller p_136036_) {
        RegistryOps<JsonElement> registryops = this.registries.createSerializationContext(JsonOps.INSTANCE);
        Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
        p_136034_.forEach((p_326203_, p_326204_) -> {
            try {
                var json = net.minecraftforge.common.ForgeHooks.readConditionalAdvancement(registryops, (com.google.gson.JsonObject)p_326204_);
                if (json == null) {
                    LOGGER.debug("Skipping loading advancement {} as its conditions were not met", p_326204_);
                    return;
                }
                Advancement advancement = Advancement.CODEC.parse(registryops, json).getOrThrow(JsonParseException::new);
                this.validate(p_326203_, advancement);
                builder.put(p_326203_, new AdvancementHolder(p_326203_, advancement));
            } catch (Exception exception) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", p_326203_, exception.getMessage());
            }
        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementtree = new AdvancementTree();
        advancementtree.addAll(this.advancements.values());

        for (AdvancementNode advancementnode : advancementtree.roots()) {
            if (advancementnode.holder().value().display().isPresent()) {
                TreeNodePosition.run(advancementnode);
            }
        }

        this.tree = advancementtree;
    }

    private void validate(ResourceLocation p_309906_, Advancement p_310937_) {
        ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
        p_310937_.validate(problemreporter$collector, this.registries.asGetterLookup());
        problemreporter$collector.getReport().ifPresent(p_341121_ -> LOGGER.warn("Found validation problems in advancement {}: \n{}", p_309906_, p_341121_));
    }

    @Nullable
    public AdvancementHolder get(ResourceLocation p_299615_) {
        return this.advancements.get(p_299615_);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}
