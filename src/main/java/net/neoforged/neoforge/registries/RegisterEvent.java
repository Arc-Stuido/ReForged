package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

/**
 * Wrapper around Forge's {@link net.minecraftforge.registries.RegisterEvent}.
 * NeoForge mods use {@code event.register(registryKey, helper -> ...)} to register objects.
 */
public class RegisterEvent {
    private final net.minecraftforge.registries.RegisterEvent delegate;

    public RegisterEvent(net.minecraftforge.registries.RegisterEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * Register objects for the given registry. The consumer receives a helper
     * whose {@link RegisterHelper#register} method adds entries.
     */
    @SuppressWarnings("unchecked")
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey,
                             Consumer<RegisterHelper<T>> consumer) {
        delegate.register(registryKey, forgeHelper -> {
            RegisterHelper<T> neoHelper = (id, value) -> forgeHelper.register(id, value);
            consumer.accept(neoHelper);
        });
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {
        void register(ResourceLocation id, T value);
    }
}
