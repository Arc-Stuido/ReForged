package org.xiyu.reforged.mixin;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.minecraftforge.registries.NewRegistryEvent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the NeoForge RegistryBuilder overload to Forge's NewRegistryEvent after
 * package remapping rewrites NeoForge event references to Forge.
 */
@Mixin(value = NewRegistryEvent.class, remap = false)
public abstract class NewRegistryEventMixin {

    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        return builder.create();
    }
}
