package net.neoforged.neoforge.capabilities;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import net.neoforged.fml.event.IModBusEvent;

/**
 * NeoForge's RegisterCapabilitiesEvent — mod bus event for registering capability providers.
 * <p>Bridges NeoForge capability registration into the proxy capability classes.</p>
 */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Cached reflective access to BlockEntityType.validBlocks (NeoForge adds getValidBlocks(), Forge doesn't) */
    private static final Field VALID_BLOCKS_FIELD;
    static {
        Field f = null;
        try {
            f = BlockEntityType.class.getDeclaredField("validBlocks");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("[ReForged] Could not find BlockEntityType.validBlocks field — registerBlockEntity will be limited", e);
        }
        VALID_BLOCKS_FIELD = f;
    }

    public RegisterCapabilitiesEvent() {}

    // ─── BLOCKS ───────────────────────────────────────────────────

    /**
     * Register a capability provider for some blocks.
     */
    public <T, C extends @Nullable Object> void registerBlock(
            BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        Objects.requireNonNull(provider);
        if (blocks.length == 0) throw new IllegalArgumentException("Must register at least one block");
        for (Block block : blocks) {
            Objects.requireNonNull(block);
            capability.providers.computeIfAbsent(block, k -> new ArrayList<>()).add(provider);
        }
    }

    /**
     * Register a capability provider for a block entity type.
     * Adapts the ICapabilityProvider into IBlockCapabilityProvider by resolving
     * the block entity from the level.
     */
    @SuppressWarnings("unchecked")
    public <T, C extends @Nullable Object, BE extends BlockEntity> void registerBlockEntity(
            BlockCapability<T, C> capability, BlockEntityType<BE> blockEntityType,
            ICapabilityProvider<? super BE, C, T> provider) {
        Objects.requireNonNull(provider);

        IBlockCapabilityProvider<T, C> adaptedProvider = (level, pos, state, blockEntity, context) -> {
            if (blockEntity == null || blockEntity.getType() != blockEntityType)
                return null;
            return provider.getCapability((BE) blockEntity, context);
        };

        Set<Block> validBlocks = getValidBlocks(blockEntityType);
        if (validBlocks.isEmpty()) {
            LOGGER.warn("[ReForged] No valid blocks found for BlockEntityType {} — capability {} not registered for any block",
                    blockEntityType, capability.name());
            return;
        }

        for (Block block : validBlocks) {
            Objects.requireNonNull(block);
            capability.providers.computeIfAbsent(block, k -> new ArrayList<>()).add(adaptedProvider);
        }
    }

    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        Objects.requireNonNull(block);
        return capability.providers.containsKey(block);
    }

    // ─── ENTITIES ─────────────────────────────────────────────────

    /**
     * Register a capability provider for an entity type.
     */
    @SuppressWarnings("unchecked")
    public <T, C extends @Nullable Object, E extends Entity> void registerEntity(
            EntityCapability<T, C> capability, EntityType<E> entityType,
            ICapabilityProvider<? super E, C, T> provider) {
        Objects.requireNonNull(provider);
        capability.providers.computeIfAbsent(entityType, et -> new ArrayList<>())
                .add((ICapabilityProvider<Entity, C, T>) provider);
    }

    public boolean isEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> entityType) {
        Objects.requireNonNull(entityType);
        return capability.providers.containsKey(entityType);
    }

    // ─── ITEMS ────────────────────────────────────────────────────

    /**
     * Register a capability provider for some items.
     */
    public <T, C extends @Nullable Object> void registerItem(
            ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider,
            ItemLike... items) {
        Objects.requireNonNull(provider);
        if (items.length == 0) throw new IllegalArgumentException("Must register at least one item");
        for (ItemLike itemLike : items) {
            Item item = Objects.requireNonNull(itemLike.asItem());
            capability.providers.computeIfAbsent(item, k -> new ArrayList<>()).add(provider);
        }
    }

    public boolean isItemRegistered(ItemCapability<?, ?> capability, Item item) {
        Objects.requireNonNull(item);
        return capability.providers.containsKey(item);
    }

    // ─── Reflection helper ────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static <BE extends BlockEntity> Set<Block> getValidBlocks(BlockEntityType<BE> blockEntityType) {
        if (VALID_BLOCKS_FIELD == null) return Collections.emptySet();
        try {
            return (Set<Block>) VALID_BLOCKS_FIELD.get(blockEntityType);
        } catch (IllegalAccessException e) {
            LOGGER.error("[ReForged] Failed to read validBlocks from BlockEntityType", e);
            return Collections.emptySet();
        }
    }
}
