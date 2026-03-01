package net.minecraft.client.multiplayer;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SessionSearchTrees {
    private static final SessionSearchTrees.Key RECIPE_COLLECTIONS = new SessionSearchTrees.Key();
    public static final SessionSearchTrees.Key CREATIVE_NAMES = new SessionSearchTrees.Key();
    public static final SessionSearchTrees.Key CREATIVE_TAGS = new SessionSearchTrees.Key();
    private CompletableFuture<SearchTree<ItemStack>> EMPTY = CompletableFuture.completedFuture(SearchTree.empty());
    private Map<SessionSearchTrees.Key, CompletableFuture<SearchTree<ItemStack>>> creativeSearch = new IdentityHashMap<>();
    private CompletableFuture<SearchTree<RecipeCollection>> recipeSearch = CompletableFuture.completedFuture(SearchTree.empty());
    private final Map<SessionSearchTrees.Key, Runnable> reloaders = new IdentityHashMap<>();

    private void register(SessionSearchTrees.Key p_342736_, Runnable p_343077_) {
        p_343077_.run();
        this.reloaders.put(p_342736_, p_343077_);
    }

    public void rebuildAfterLanguageChange() {
        for (Runnable runnable : this.reloaders.values()) {
            runnable.run();
        }
    }

    private static Stream<String> getTooltipLines(Stream<ItemStack> p_344293_, Item.TooltipContext p_343228_, TooltipFlag p_342315_) {
        return p_344293_.<Component>flatMap(p_343071_ -> p_343071_.getTooltipLines(p_343228_, null, p_342315_).stream())
            .map(p_344266_ -> ChatFormatting.stripFormatting(p_344266_.getString()).trim())
            .filter(p_345189_ -> !p_345189_.isEmpty());
    }

    public void updateRecipes(ClientRecipeBook p_343609_, RegistryAccess.Frozen p_342630_) {
        this.register(
            RECIPE_COLLECTIONS,
            () -> {
                List<RecipeCollection> list = p_343609_.getCollections();
                Registry<Item> registry = p_342630_.registryOrThrow(Registries.ITEM);
                Item.TooltipContext item$tooltipcontext = Item.TooltipContext.of(p_342630_);
                TooltipFlag tooltipflag = TooltipFlag.Default.NORMAL;
                CompletableFuture<?> completablefuture = this.recipeSearch;
                this.recipeSearch = CompletableFuture.supplyAsync(
                    () -> new FullTextSearchTree<>(
                            p_342843_ -> getTooltipLines(
                                    p_342843_.getRecipes().stream().map(p_342648_ -> p_342648_.value().getResultItem(p_342630_)), item$tooltipcontext, tooltipflag
                                ),
                            p_342068_ -> p_342068_.getRecipes().stream().map(p_342436_ -> registry.getKey(p_342436_.value().getResultItem(p_342630_).getItem())),
                            list
                        ),
                    Util.backgroundExecutor()
                );
                completablefuture.cancel(true);
            }
        );
    }

    public SearchTree<RecipeCollection> recipes() {
        return this.recipeSearch.join();
    }

    public void updateCreativeTags(List<ItemStack> p_344581_) {
        for (var entry : net.minecraftforge.client.CreativeModeTabSearchRegistry.getTagSearchKeys().entrySet())
        this.register(
            entry.getValue(),
            () -> {
                var items = entry.getKey() == net.minecraft.world.item.CreativeModeTabs.searchTab() ? p_344581_ : List.copyOf(entry.getKey().getDisplayItems());
                CompletableFuture<?> completablefuture = this.creativeSearch.getOrDefault(entry.getValue(), EMPTY);
                this.creativeSearch.put(entry.getValue(), CompletableFuture.supplyAsync(
                    () -> new IdSearchTree<>(p_342206_ -> p_342206_.getTags().map(TagKey::location), items), Util.backgroundExecutor()
                ));
                completablefuture.cancel(true);
            }
        );
    }

    public SearchTree<ItemStack> creativeTagSearch() {
        return this.getSearchTree(CREATIVE_TAGS);
    }

    public void updateCreativeTooltips(HolderLookup.Provider p_343364_, List<ItemStack> p_342500_) {
        for (var entry : net.minecraftforge.client.CreativeModeTabSearchRegistry.getNameSearchKeys().entrySet())
        this.register(
            entry.getValue(),
            () -> {
                var items = entry.getKey() == net.minecraft.world.item.CreativeModeTabs.searchTab() ? p_342500_ : List.copyOf(entry.getKey().getDisplayItems());
                Item.TooltipContext item$tooltipcontext = Item.TooltipContext.of(p_343364_);
                TooltipFlag tooltipflag = TooltipFlag.Default.NORMAL.asCreative();
                CompletableFuture<?> completablefuture = this.creativeSearch.getOrDefault(entry.getValue(), EMPTY);
                this.creativeSearch.put(entry.getValue(), CompletableFuture.supplyAsync(
                    () -> new FullTextSearchTree<>(
                            p_345254_ -> getTooltipLines(Stream.of(p_345254_), item$tooltipcontext, tooltipflag),
                            p_344415_ -> p_344415_.getItemHolder().unwrapKey().map(ResourceKey::location).stream(),
                            items
                        ),
                    Util.backgroundExecutor()
                ));
                completablefuture.cancel(true);
            }
        );
    }

    public SearchTree<ItemStack> creativeNameSearch() {
        return this.getSearchTree(CREATIVE_NAMES);
    }

    public SearchTree<ItemStack> getSearchTree(SessionSearchTrees.Key key) {
        return this.creativeSearch.getOrDefault(key, EMPTY).join();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Key {
    }
}
