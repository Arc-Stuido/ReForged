package net.neoforged.neoforge.registries.datamaps;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: NeoForge's RegisterDataMapTypesEvent.
 */
public class RegisterDataMapTypesEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> attachments;

    public RegisterDataMapTypesEvent() {
        this.attachments = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T, R> void register(DataMapType<R, T> type) {
        final var map = attachments.computeIfAbsent((ResourceKey) type.registryKey(), k -> new HashMap<>());
        map.put(type.id(), type);
    }
}
