package net.neoforged.neoforge.fluids;

import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * Stub: NeoForge's IFluidTank interface.
 */
public interface IFluidTank {
    FluidStack getFluid();
    int getFluidAmount();
    int getCapacity();
    boolean isFluidValid(FluidStack stack);
    int fill(FluidStack resource, FluidAction action);
    FluidStack drain(int maxDrain, FluidAction action);
    FluidStack drain(FluidStack resource, FluidAction action);
}
