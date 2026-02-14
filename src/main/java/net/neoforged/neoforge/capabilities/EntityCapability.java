package net.neoforged.neoforge.capabilities;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's EntityCapability — capability lookup for entities.
 */
public final class EntityCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    private static final Map<ResourceLocation, EntityCapability<?, ?>> registry = new ConcurrentHashMap<>();

    final Map<EntityType<?>, List<ICapabilityProvider<Entity, C, T>>> providers = new IdentityHashMap<>();

    private EntityCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    @SuppressWarnings("unchecked")
    public static <T, C extends @Nullable Object> EntityCapability<T, C> create(
            ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (EntityCapability<T, C>) registry.computeIfAbsent(name,
                n -> new EntityCapability<>(n, typeClass, contextClass));
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityCapability<T, @Nullable Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return (EntityCapability<T, @Nullable Void>) create(name, typeClass, (Class<@Nullable Void>) Void.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return (EntityCapability<T, @Nullable Direction>) create(name, typeClass, (Class<@Nullable Direction>) Direction.class);
    }

    public static synchronized List<EntityCapability<?, ?>> getAll() {
        return new ArrayList<>(registry.values());
    }

    @Nullable
    public T getCapability(Entity entity, C context) {
        for (var provider : providers.getOrDefault(entity.getType(), List.of())) {
            var ret = provider.getCapability(entity, context);
            if (ret != null) return ret;
        }
        return null;
    }
}
