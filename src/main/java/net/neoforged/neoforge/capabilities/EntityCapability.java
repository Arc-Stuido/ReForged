package net.neoforged.neoforge.capabilities;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.xiyu.reforged.shim.capabilities.ForgeCapabilityBridge;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * NeoForge's EntityCapability — capability lookup for entities.
 * <p>Falls back to Forge's capability system when no NeoForge provider is registered.</p>
 */
public final class EntityCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    private static final Logger LOGGER = LogUtils.getLogger();
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

    /**
     * Query an entity capability.
     * <ol>
     *   <li>First checks registered NeoForge-style providers</li>
     *   <li>Falls back to Forge's ICapabilityProvider on the Entity</li>
     * </ol>
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public T getCapability(Entity entity, C context) {
        if (entity == null) return null;

        // 1. Try NeoForge-style providers
        for (var provider : providers.getOrDefault(entity.getType(), List.of())) {
            var ret = provider.getCapability(entity, context);
            if (ret != null) return ret;
        }

        // 2. Fall back to Forge capability system
        try {
            Capability<T> forgeCap = ForgeCapabilityBridge.findForgeCapability(name(), typeClass());
            if (forgeCap != null) {
                Direction side = (context instanceof Direction d) ? d : null;
                LazyOptional<T> lazyOpt = side != null
                        ? entity.getCapability(forgeCap, side)
                        : entity.getCapability(forgeCap);
                if (lazyOpt.isPresent()) {
                    return lazyOpt.orElse(null);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] EntityCapability Forge fallback failed for {}: {}", name(), e.getMessage());
        }
        return null;
    }
}
