package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * Proxy for NeoForge's {@code DeferredRegister}.
 * Uses composition to wrap Forge's DeferredRegister, since constructor signatures differ.
 *
 * <p>NeoForge mods use {@code DeferredRegister.create(Registries.BLOCKS, MODID)};
 * we translate to Forge's static factory methods.</p>
 *
 * <h3>No-op mode</h3>
 * <p>For NeoForge-only registries that have no Forge equivalent
 * (e.g. {@code neoforge:attachment_types}), the register uses <em>no-op mode</em>:
 * entries are stored locally and their suppliers are evaluated lazily.
 * {@link #register(IEventBus)} becomes a no-op so Forge never tries to look up the
 * non-existent registry.</p>
 */
public class DeferredRegister<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Known registry namespaces that exist in Forge and can be safely delegated. */
    private static final Set<String> FORGE_KNOWN_NAMESPACES = Set.of("minecraft", "forge");

    /** NeoForge-only registry paths that explicitly have no Forge equivalent. */
    private static final Set<String> NEOFORGE_ONLY_REGISTRIES = Set.of(
            "neoforge:attachment_types"
    );

    protected final net.minecraftforge.registries.DeferredRegister<T> delegate; // null in no-op mode
    private final String modid;
    private final ResourceKey<? extends Registry<T>> registryKey; // stored for key generation
    protected boolean isNoOp;

    protected DeferredRegister(net.minecraftforge.registries.DeferredRegister<T> delegate,
                               String modid, boolean isNoOp, ResourceKey<? extends Registry<T>> registryKey) {
        this.delegate = delegate;
        this.modid = modid;
        this.isNoOp = isNoOp;
        this.registryKey = registryKey;
    }

    /* ---------- Namespace remapping ---------- */

    /**
     * Remap NeoForge-specific registry namespaces to Forge equivalents.
     * e.g. {@code neoforge:global_loot_modifier_serializers} → {@code forge:global_loot_modifier_serializers}
     */
    private static ResourceLocation remapRegistryName(ResourceLocation name) {
        if ("neoforge".equals(name.getNamespace())) {
            return ResourceLocation.fromNamespaceAndPath("forge", name.getPath());
        }
        return name;
    }

    /**
     * Check if a registry name (after remapping) should use no-op mode.
     * Returns true for mod-defined custom registries and NeoForge-only registries.
     */
    private static boolean shouldBeNoOp(ResourceLocation originalName, ResourceLocation remappedName) {
        // Explicit NeoForge-only registries
        if (NEOFORGE_ONLY_REGISTRIES.contains(originalName.toString())) {
            return true;
        }
        // After remapping, only minecraft: and forge: registries exist in Forge.
        // Everything else (mod custom registries like champions:*, etc.) → no-op
        return !FORGE_KNOWN_NAMESPACES.contains(remappedName.getNamespace());
    }

    /* ---------- Factory methods ---------- */

    /**
     * NeoForge-style factory with ResourceKey.
     */
    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        ResourceLocation registryName = registryKey.location();
        ResourceLocation remapped = remapRegistryName(registryName);

        if (shouldBeNoOp(registryName, remapped)) {
            LOGGER.info("[ReForged] Registry '{}' has no Forge equivalent. Using no-op DeferredRegister for mod '{}'",
                    registryName, modid);
            return new DeferredRegister<>(null, modid, true, registryKey);
        }

        net.minecraftforge.registries.DeferredRegister<T> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(remapped, modid);
        return new DeferredRegister<>(forgeReg, modid, false, registryKey);
    }

    /**
     * NeoForge-style factory with IForgeRegistry.
     */
    public static <B> DeferredRegister<B> create(IForgeRegistry<B> registry, String modid) {
        net.minecraftforge.registries.DeferredRegister<B> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(registry, modid);
        return new DeferredRegister<>(forgeReg, modid, false, null);
    }

    /**
     * NeoForge-style factory with vanilla Registry object.
     */
    public static <B> DeferredRegister<B> create(Registry<B> registry, String modid) {
        return create(registry.key(), modid);
    }

    /**
     * NeoForge convenience factory with ResourceLocation.
     */
    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceLocation registryName, String modid) {
        ResourceLocation remapped = remapRegistryName(registryName);

        // Construct a registry key for tracking
        ResourceKey<Registry<T>> rk = (ResourceKey<Registry<T>>) (ResourceKey<?>) ResourceKey.createRegistryKey(registryName);

        if (shouldBeNoOp(registryName, remapped)) {
            LOGGER.info("[ReForged] Registry '{}' has no Forge equivalent. Using no-op DeferredRegister for mod '{}'",
                    registryName, modid);
            return new DeferredRegister<>(null, modid, true, rk);
        }

        net.minecraftforge.registries.DeferredRegister<T> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(remapped, modid);
        return new DeferredRegister<>(forgeReg, modid, false, rk);
    }

    /* ---------- Registration ---------- */

    /**
     * Register an entry — returns a DeferredHolder (NeoForge's RegistryObject).
     * In no-op mode, returns a direct holder backed by the supplier.
     * The supplier is also stored as a fallback for late registration support.
     */
    @SuppressWarnings("unchecked")
    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        if (isNoOp) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
            LOGGER.debug("[ReForged] No-op register: {}", id);
            if (registryKey != null) {
                // Create a full ResourceKey for this entry so getKey() works downstream (Registrate)
                ResourceKey<T> entryKey = ResourceKey.create((ResourceKey<Registry<T>>) registryKey, id);
                return (DeferredHolder<T, I>) DeferredHolder.createDirect(entryKey, sup);
            }
            return DeferredHolder.createDirect(id, sup);
        }
        LOGGER.info("[ReForged] Registering entry '{}'  for mod '{}' via Forge DeferredRegister", name, modid);
        RegistryObject<I> obj = delegate.register(name, sup);
        LOGGER.info("[ReForged] RegistryObject created: {} (id={})", obj, obj.getId());
        return DeferredHolder.wrap(obj, sup);
    }

    /**
     * Register this DeferredRegister to the given event bus.
     * In no-op mode, this is a safe no-op.
     */
    public void register(IEventBus bus) {
        if (isNoOp) {
            LOGGER.debug("[ReForged] Skipping no-op DeferredRegister.register() for mod '{}'", modid);
            return;
        }
        LOGGER.info("[ReForged] DeferredRegister.register() for mod '{}', delegate entries: {}, delegate class: {}",
                modid, delegate.getEntries().size(), delegate.getClass().getName());
        delegate.register(bus);
        LOGGER.info("[ReForged] DeferredRegister.register() completed for mod '{}'", modid);
    }

    /**
     * Get all registered entries.
     */
    public Collection<RegistryObject<T>> getEntries() {
        if (isNoOp) return Collections.emptyList();
        return delegate.getEntries();
    }

    /**
     * Get the namespace of this registry.
     */
    public String getNamespace() {
        return this.modid;
    }

    /**
     * Adds an alias that maps from one registry name to another.
     * In Forge's DeferredRegister, aliases are not directly supported, so this is a no-op.
     *
     * @param from The source registry name to alias from.
     * @param to   The target registry name to alias to.
     */
    public void addAlias(net.minecraft.resources.ResourceLocation from, net.minecraft.resources.ResourceLocation to) {
        LOGGER.debug("[ReForged] DeferredRegister.addAlias({} -> {}) for mod '{}' — no-op in Forge shim", from, to, modid);
    }

    /**
     * NeoForge's makeRegistry — creates a custom registry via a RegistryBuilder.
     * Returns a Supplier that yields the newly created registry.
     * <p>
     * Since mod-defined custom registries don't exist in Forge, calling this
     * also switches this DeferredRegister to no-op mode.
     * </p>
     */
    @SuppressWarnings("unchecked")
    public Supplier<Registry<T>> makeRegistry(Supplier<RegistryBuilder<T>> builderSup) {
        LOGGER.info("[ReForged] makeRegistry() called for mod '{}' — switching to no-op mode", modid);
        this.isNoOp = true;
        RegistryBuilder<T> builder = builderSup.get();
        Registry<T> registry = builder.build();
        return () -> registry;
    }

    /* ---------- DataComponents specialisation ---------- */

    /**
     * NeoForge's DataComponents specialization of DeferredRegister.
     * Provides {@code registerComponentType()} for NeoForge mods that register
     * custom data component types via a builder pattern.
     */
    public static class DataComponents extends DeferredRegister<net.minecraft.core.component.DataComponentType<?>> {
        @SuppressWarnings("unchecked")
        protected DataComponents(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, modid), modid, false,
                    (ResourceKey<? extends Registry<net.minecraft.core.component.DataComponentType<?>>>) (ResourceKey<?>) net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);
        }

        public static DataComponents createDataComponents(String modid) {
            return new DataComponents(modid);
        }

        /**
         * Register a new {@link net.minecraft.core.component.DataComponentType} using a builder.
         *
         * <p>NeoForge mods call this as:
         * <pre>{@code
         * DATA_COMPONENTS.registerComponentType("my_component",
         *     builder -> builder.persistent(MyCodec.CODEC).networkSynchronized(MyCodec.STREAM_CODEC));
         * }</pre>
         *
         * @param name            the registry name
         * @param builderOperator configures the {@code DataComponentType.Builder}
         * @return a DeferredHolder for the registered component type
         */
        @SuppressWarnings("unchecked")
        public <T> DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>>
        registerComponentType(String name, java.util.function.UnaryOperator<net.minecraft.core.component.DataComponentType.Builder<T>> builderOperator) {
            return (DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>>)
                    (DeferredHolder<?, ?>) this.register(name, () -> {
                        net.minecraft.core.component.DataComponentType.Builder<T> builder =
                                net.minecraft.core.component.DataComponentType.builder();
                        builder = builderOperator.apply(builder);
                        return builder.build();
                    });
        }
    }

    /**
     * NeoForge factory for DataComponents.
     */
    public static DataComponents createDataComponents(
            ResourceKey<? extends Registry<net.minecraft.core.component.DataComponentType<?>>> registryKey,
            String modid) {
        return new DataComponents(modid);
    }

    /* ---------- Items specialisation ---------- */

    /**
     * NeoForge's Items specialization of DeferredRegister.
     * Provides {@code registerSimpleItem()}, {@code registerItem()}, and
     * {@code registerSimpleBlockItem()} for NeoForge mods that register items.
     */
    public static class Items extends DeferredRegister<net.minecraft.world.item.Item> {

        protected Items(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.ITEM, modid), modid, false,
                    net.minecraft.core.registries.Registries.ITEM);
        }

        /**
         * Factory method matching NeoForge's {@code DeferredRegister.createItems(String)}.
         */
        public static Items createItems(String modid) {
            return new Items(modid);
        }

        /**
         * Override register to return DeferredItem instead of DeferredHolder.
         * This is required because NeoForge mods expect Items.register() to return DeferredItem.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.item.Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            if (isNoOp) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
                return DeferredItem.createDirectItem(id, sup);
            }
            RegistryObject<I> obj = delegate.register(name, sup);
            return DeferredItem.wrapItem(obj, sup);
        }

        /**
         * Register a simple item with default properties.
         */
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item>
        registerSimpleItem(String name) {
            return registerSimpleItem(name, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple item with specified properties.
         */
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item>
        registerSimpleItem(String name, net.minecraft.world.item.Item.Properties properties) {
            return registerItem(name, p -> new net.minecraft.world.item.Item(p), properties);
        }

        /**
         * Register an item using a factory function and default properties.
         */
        public <I extends net.minecraft.world.item.Item> DeferredHolder<net.minecraft.world.item.Item, I>
        registerItem(String name, java.util.function.Function<net.minecraft.world.item.Item.Properties, I> factory) {
            return registerItem(name, factory, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register an item using a factory function and specified properties.
         */
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.item.Item> DeferredHolder<net.minecraft.world.item.Item, I>
        registerItem(String name, java.util.function.Function<net.minecraft.world.item.Item.Properties, I> factory,
                     net.minecraft.world.item.Item.Properties properties) {
            return (DeferredHolder<net.minecraft.world.item.Item, I>)
                    (DeferredHolder<?, ?>) this.register(name, () -> factory.apply(properties));
        }

        /**
         * Register a simple BlockItem for a given block.
         */
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.world.level.block.Block block) {
            return registerSimpleBlockItem(block, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple BlockItem for a given block with given properties.
         */
        @SuppressWarnings("unchecked")
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.world.level.block.Block block, net.minecraft.world.item.Item.Properties properties) {
            return (DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>)
                    (DeferredHolder<?, ?>) this.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath(),
                            () -> new net.minecraft.world.item.BlockItem(block, properties));
        }

        /**
         * Register a simple BlockItem using a DeferredHolder for the block.
         */
        @SuppressWarnings("unchecked")
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(DeferredHolder<net.minecraft.world.level.block.Block, ? extends net.minecraft.world.level.block.Block> blockHolder) {
            return (DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>)
                    (DeferredHolder<?, ?>) this.register(blockHolder.getId().getPath(),
                            () -> new net.minecraft.world.item.BlockItem(blockHolder.get(), new net.minecraft.world.item.Item.Properties()));
        }

        /**
         * Register a simple BlockItem using a DeferredHolder for the block with given properties.
         */
        @SuppressWarnings("unchecked")
        public DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(DeferredHolder<net.minecraft.world.level.block.Block, ? extends net.minecraft.world.level.block.Block> blockHolder,
                                net.minecraft.world.item.Item.Properties properties) {
            return (DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.BlockItem>)
                    (DeferredHolder<?, ?>) this.register(blockHolder.getId().getPath(),
                            () -> new net.minecraft.world.item.BlockItem(blockHolder.get(), properties));
        }
    }

    /**
     * NeoForge factory for Items.
     */
    public static Items createItems(String modid) {
        return new Items(modid);
    }

    /* ---------- Blocks specialisation ---------- */

    /**
     * NeoForge's Blocks specialization of DeferredRegister.
     * Provides {@code registerBlock()}, {@code registerSimpleBlock()}, and
     * returns {@link DeferredBlock} from {@code register()}.
     */
    public static class Blocks extends DeferredRegister<net.minecraft.world.level.block.Block> {

        protected Blocks(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.BLOCK, modid), modid, false,
                    net.minecraft.core.registries.Registries.BLOCK);
        }

        /**
         * Factory method matching NeoForge's {@code DeferredRegister.createBlocks(String)}.
         */
        public static Blocks createBlocks(String modid) {
            return new Blocks(modid);
        }

        /**
         * Override register to return DeferredBlock instead of DeferredHolder.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.level.block.Block> DeferredBlock<I> register(String name, Supplier<? extends I> sup) {
            if (isNoOp) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
                return new DeferredBlock<>(id, sup);
            }
            RegistryObject<I> obj = delegate.register(name, sup);
            return new DeferredBlock<>(obj, sup);
        }

        /**
         * Register a block using a factory function and specified properties.
         */
        @SuppressWarnings("unchecked")
        public <B extends net.minecraft.world.level.block.Block> DeferredBlock<B>
        registerBlock(String name,
                      java.util.function.Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, ? extends B> func,
                      net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
            return (DeferredBlock<B>) (DeferredBlock<?>) this.register(name, () -> func.apply(props));
        }

        /**
         * Register a block using a factory function and default properties.
         */
        public <B extends net.minecraft.world.level.block.Block> DeferredBlock<B>
        registerBlock(String name,
                      java.util.function.Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, ? extends B> func) {
            return this.registerBlock(name, func, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
        }

        /**
         * Register a simple block with specified properties.
         */
        public DeferredBlock<net.minecraft.world.level.block.Block>
        registerSimpleBlock(String name, net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
            return this.registerBlock(name, net.minecraft.world.level.block.Block::new, props);
        }

        /**
         * Register a simple block with default properties.
         */
        public DeferredBlock<net.minecraft.world.level.block.Block>
        registerSimpleBlock(String name) {
            return this.registerSimpleBlock(name, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
        }
    }

    /**
     * NeoForge factory for Blocks.
     */
    public static Blocks createBlocks(String modid) {
        return new Blocks(modid);
    }
}
