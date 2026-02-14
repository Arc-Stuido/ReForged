package net.neoforged.neoforge.common.data;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Stub: NeoForge LanguageProvider — data provider for language (translation) files.
 */
public abstract class LanguageProvider implements DataProvider {
    private final Map<String, String> data = new TreeMap<>();
    private final PackOutput output;
    private final String modid;
    private final String locale;

    public LanguageProvider(PackOutput output, String modid, String locale) {
        this.output = output;
        this.modid = modid;
        this.locale = locale;
    }

    protected abstract void addTranslations();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        addTranslations();
        return CompletableFuture.completedFuture(null);
    }

    public void addBlock(Supplier<? extends Block> key, String name) {}
    public void add(Block key, String name) {}
    public void addItem(Supplier<? extends Item> key, String name) {}
    public void add(Item key, String name) {}
    public void addItemStack(Supplier<ItemStack> key, String name) {}
    public void add(ItemStack key, String name) {}
    public void addEffect(Supplier<? extends MobEffect> key, String name) {}
    public void add(MobEffect key, String name) {}
    public void addEntityType(Supplier<? extends EntityType<?>> key, String name) {}
    public void add(EntityType<?> key, String name) {}
    public void addTag(Supplier<? extends TagKey<?>> key, String name) {}
    public void add(TagKey<?> tagKey, String name) {}
    public void add(String key, String value) { data.put(key, value); }
    public void addDimension(ResourceKey<Level> dimension, String value) {}

    @Override
    public String getName() {
        return "Languages: " + modid;
    }
}
