package net.neoforged.neoforge.common.util;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;

/**
 * Stub: Dummy saved data that does nothing.
 */
public class DummySavedData extends SavedData {
    public static final DummySavedData INSTANCE = new DummySavedData();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return tag;
    }
}
