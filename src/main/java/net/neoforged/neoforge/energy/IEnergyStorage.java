package net.neoforged.neoforge.energy;

/**
 * Stub: NeoForge's IEnergyStorage interface.
 */
public interface IEnergyStorage {
    int receiveEnergy(int toReceive, boolean simulate);
    int extractEnergy(int toExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
