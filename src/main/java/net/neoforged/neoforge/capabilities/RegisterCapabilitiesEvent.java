package net.neoforged.neoforge.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's RegisterCapabilitiesEvent — mod bus event for registering capability providers.
 */
public class RegisterCapabilitiesEvent extends Event {

    public RegisterCapabilitiesEvent() {}

    // BLOCKS
    public <T, C extends @Nullable Object> void registerBlock(
            BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        for (Block block : blocks) {
            capability.providers.computeIfAbsent(block, k -> new ArrayList<>()).add(provider);
        }
    }

    public <T, C extends @Nullable Object, BE extends BlockEntity> void registerBlockEntity(
            BlockCapability<T, C> capability, BlockEntityType<BE> blockEntityType,
            ICapabilityProvider<? super BE, C, T> provider) {
        // No-op stub
    }

    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        return capability.providers.containsKey(block);
    }

    // ENTITIES
    public <T, C extends @Nullable Object, E extends Entity> void registerEntity(
            EntityCapability<T, C> capability, EntityType<E> entityType,
            ICapabilityProvider<? super E, C, T> provider) {
        // No-op stub
    }

    public boolean isEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> entityType) {
        return capability.providers.containsKey(entityType);
    }

    // ITEMS
    public <T, C extends @Nullable Object> void registerItem(
            ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider,
            ItemLike... items) {
        // No-op stub
    }
}
