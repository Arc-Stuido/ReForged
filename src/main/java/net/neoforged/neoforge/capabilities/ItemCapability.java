package net.neoforged.neoforge.capabilities;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's ItemCapability — capability lookup for items.
 */
public final class ItemCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    private static final Map<ResourceLocation, ItemCapability<?, ?>> registry = new ConcurrentHashMap<>();

    final Map<Item, List<ICapabilityProvider<ItemStack, C, T>>> providers = new IdentityHashMap<>();

    private ItemCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    @SuppressWarnings("unchecked")
    public static <T, C extends @Nullable Object> ItemCapability<T, C> create(
            ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (ItemCapability<T, C>) registry.computeIfAbsent(name,
                n -> new ItemCapability<>(n, typeClass, contextClass));
    }

    @SuppressWarnings("unchecked")
    public static <T> ItemCapability<T, @Nullable Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return (ItemCapability<T, @Nullable Void>) create(name, typeClass, (Class<@Nullable Void>) Void.class);
    }

    public static synchronized List<ItemCapability<?, ?>> getAll() {
        return new ArrayList<>(registry.values());
    }

    @Nullable
    public T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) return null;
        for (var provider : providers.getOrDefault(stack.getItem(), List.of())) {
            var ret = provider.getCapability(stack, context);
            if (ret != null) return ret;
        }
        return null;
    }
}
