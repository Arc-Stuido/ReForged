package net.neoforged.neoforge.registries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.HashMap;

/**
 * Stub: Registry snapshot for syncing registries between server and client.
 */
public class RegistrySnapshot {
    private final Map<ResourceLocation, Integer> ids = new HashMap<>();

    public RegistrySnapshot() {}

    public Map<ResourceLocation, Integer> getIds() {
        return ids;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ids.forEach((key, value) -> tag.putInt(key.toString(), value));
        return tag;
    }

    public static RegistrySnapshot read(CompoundTag tag) {
        RegistrySnapshot snapshot = new RegistrySnapshot();
        for (String key : tag.getAllKeys()) {
            snapshot.ids.put(ResourceLocation.parse(key), tag.getInt(key));
        }
        return snapshot;
    }
}
