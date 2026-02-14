package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public record ParrotImitation(SoundEvent sound) {
    public static final Codec<ParrotImitation> CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec()
            .xmap(ParrotImitation::new, ParrotImitation::sound);
}
