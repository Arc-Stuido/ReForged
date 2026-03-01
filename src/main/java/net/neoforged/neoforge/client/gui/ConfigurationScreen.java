package net.neoforged.neoforge.client.gui;

import net.neoforged.fml.ModContainer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Proxy: NeoForge ConfigurationScreen stub.
 * Balm/Waystones instantiate this via: new ConfigurationScreen(ModContainer, Screen)
 */
public class ConfigurationScreen extends Screen {
    public ConfigurationScreen(ModContainer container, Screen parent) {
        super(net.minecraft.network.chat.Component.empty());
    }
}
