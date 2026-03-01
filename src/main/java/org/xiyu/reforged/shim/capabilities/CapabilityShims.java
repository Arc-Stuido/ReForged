package org.xiyu.reforged.shim.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * @deprecated The capability system now uses the proxy layer in
 * {@code net.neoforged.neoforge.capabilities} directly (with Forge bridge).
 * This class is kept for backward compatibility with any shim-route references
 * but delegates to the proxy classes.
 */
@Deprecated
public final class CapabilityShims {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ─── BlockCapability (delegates to proxy) ─────────────────────
    public static final class BlockCapability<T, C> {
        private final net.neoforged.neoforge.capabilities.BlockCapability<T, ?> delegate;

        private BlockCapability(net.neoforged.neoforge.capabilities.BlockCapability<T, ?> delegate) {
            this.delegate = delegate;
        }

        @SuppressWarnings("unchecked")
        public static <T, C> BlockCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            var proxy = net.neoforged.neoforge.capabilities.BlockCapability.create(name, typeClass, contextClass);
            return new BlockCapability<>(proxy);
        }

        public static <T> BlockCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        public static <T> BlockCapability<T, Direction> createSided(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Direction.class);
        }

        public ResourceLocation name() { return delegate.name(); }
        public Class<T> typeClass() { return delegate.typeClass(); }
    }

    // ─── EntityCapability (delegates to proxy) ────────────────────
    public static final class EntityCapability<T, C> {
        private final net.neoforged.neoforge.capabilities.EntityCapability<T, ?> delegate;

        private EntityCapability(net.neoforged.neoforge.capabilities.EntityCapability<T, ?> delegate) {
            this.delegate = delegate;
        }

        @SuppressWarnings("unchecked")
        public static <T, C> EntityCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            var proxy = net.neoforged.neoforge.capabilities.EntityCapability.create(name, typeClass, contextClass);
            return new EntityCapability<>(proxy);
        }

        public static <T> EntityCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        public ResourceLocation name() { return delegate.name(); }
    }

    // ─── ItemCapability (delegates to proxy) ──────────────────────
    public static final class ItemCapability<T, C> {
        private final net.neoforged.neoforge.capabilities.ItemCapability<T, ?> delegate;

        private ItemCapability(net.neoforged.neoforge.capabilities.ItemCapability<T, ?> delegate) {
            this.delegate = delegate;
        }

        @SuppressWarnings("unchecked")
        public static <T, C> ItemCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            var proxy = net.neoforged.neoforge.capabilities.ItemCapability.create(name, typeClass, contextClass);
            return new ItemCapability<>(proxy);
        }

        public static <T> ItemCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        public ResourceLocation name() { return delegate.name(); }
    }

    // ─── Capabilities built-in constants (delegates to proxy) ─────
    public static final class Capabilities {
        public static final class ItemHandler {
            public static final net.neoforged.neoforge.capabilities.BlockCapability<?, ?> BLOCK =
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK;
            public static final net.neoforged.neoforge.capabilities.EntityCapability<?, ?> ENTITY =
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ENTITY;
            public static final net.neoforged.neoforge.capabilities.ItemCapability<?, ?> ITEM =
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM;
        }

        public static final class FluidHandler {
            public static final net.neoforged.neoforge.capabilities.BlockCapability<?, ?> BLOCK =
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK;
            public static final net.neoforged.neoforge.capabilities.EntityCapability<?, ?> ENTITY =
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ENTITY;
            public static final net.neoforged.neoforge.capabilities.ItemCapability<?, ?> ITEM =
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM;
        }

        public static final class EnergyStorage {
            public static final net.neoforged.neoforge.capabilities.BlockCapability<?, ?> BLOCK =
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK;
            public static final net.neoforged.neoforge.capabilities.EntityCapability<?, ?> ENTITY =
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ENTITY;
            public static final net.neoforged.neoforge.capabilities.ItemCapability<?, ?> ITEM =
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM;
        }
    }

    private CapabilityShims() {}
}
