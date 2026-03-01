package net.neoforged.neoforge.network.configuration;

import net.minecraft.resources.ResourceLocation;

/**
 * Interface for custom configuration tasks during connection.
 */
public interface ICustomConfigurationTask {
    void run(Listener listener);

    ResourceLocation id();

    @FunctionalInterface
    interface Listener {
        void finish();
    }
}
