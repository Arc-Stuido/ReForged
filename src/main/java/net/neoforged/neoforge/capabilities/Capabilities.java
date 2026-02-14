package net.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's Capabilities — built-in capability constants for ItemHandler, FluidHandler, EnergyStorage.
 */
public final class Capabilities {

    public static final class EnergyStorage {
        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK =
                BlockCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY =
                EntityCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, @Nullable Void> ITEM =
                ItemCapability.createVoid(create("energy"), IEnergyStorage.class);
        private EnergyStorage() {}
    }

    public static final class FluidHandler {
        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK =
                BlockCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY =
                EntityCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, @Nullable Void> ITEM =
                ItemCapability.createVoid(create("fluid_handler"), IFluidHandlerItem.class);
        private FluidHandler() {}
    }

    public static final class ItemHandler {
        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK =
                BlockCapability.createSided(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Void> ENTITY =
                EntityCapability.createVoid(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Direction> ENTITY_AUTOMATION =
                EntityCapability.createSided(create("item_handler_automation"), IItemHandler.class);
        public static final ItemCapability<IItemHandler, @Nullable Void> ITEM =
                ItemCapability.createVoid(create("item_handler"), IItemHandler.class);
        private ItemHandler() {}
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {}
}
