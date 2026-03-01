package net.neoforged.neoforge.client.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * NeoForge KeyConflictContext shim — delegates to Forge's
 * {@link net.minecraftforge.client.settings.KeyConflictContext}.
 */
public enum KeyConflictContext implements IKeyConflictContext {
    UNIVERSAL {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(net.minecraftforge.client.settings.IKeyConflictContext other) {
            return true;
        }
    },

    GUI {
        @Override
        public boolean isActive() {
            return Minecraft.getInstance().screen != null;
        }

        @Override
        public boolean conflicts(net.minecraftforge.client.settings.IKeyConflictContext other) {
            return this == other;
        }
    },

    IN_GAME {
        @Override
        public boolean isActive() {
            return !GUI.isActive();
        }

        @Override
        public boolean conflicts(net.minecraftforge.client.settings.IKeyConflictContext other) {
            return this == other;
        }
    };
}
