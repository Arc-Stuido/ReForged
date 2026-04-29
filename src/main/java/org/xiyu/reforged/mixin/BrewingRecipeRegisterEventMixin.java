package org.xiyu.reforged.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraftforge.event.brewing.BrewingRecipeRegisterEvent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds NeoForge's registry-access helper to Forge's renamed brewing event.
 */
@Mixin(value = BrewingRecipeRegisterEvent.class, remap = false)
public abstract class BrewingRecipeRegisterEventMixin {
    public RegistryAccess getRegistryAccess() {
        return RegistryAccess.EMPTY;
    }
}
