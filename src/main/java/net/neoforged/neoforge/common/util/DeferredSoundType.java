package net.neoforged.neoforge.common.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Supplier;

/**
 * NeoForge's DeferredSoundType — a SoundType that lazily resolves its SoundEvents
 * via Suppliers. Used by mods (e.g. Create) that register custom sounds during
 * mod loading and need to reference them before registration completes.
 */
public class DeferredSoundType extends SoundType {

    private final Supplier<SoundEvent> breakSoundSup;
    private final Supplier<SoundEvent> stepSoundSup;
    private final Supplier<SoundEvent> placeSoundSup;
    private final Supplier<SoundEvent> hitSoundSup;
    private final Supplier<SoundEvent> fallSoundSup;

    public DeferredSoundType(float volume, float pitch,
                              Supplier<SoundEvent> breakSound,
                              Supplier<SoundEvent> stepSound,
                              Supplier<SoundEvent> placeSound,
                              Supplier<SoundEvent> hitSound,
                              Supplier<SoundEvent> fallSound) {
        // Pass dummy sounds to parent — we override all getters
        super(volume, pitch, SoundEvents.EMPTY, SoundEvents.EMPTY,
                SoundEvents.EMPTY, SoundEvents.EMPTY, SoundEvents.EMPTY);
        this.breakSoundSup = breakSound;
        this.stepSoundSup = stepSound;
        this.placeSoundSup = placeSound;
        this.hitSoundSup = hitSound;
        this.fallSoundSup = fallSound;
    }

    @Override
    public SoundEvent getBreakSound() {
        return breakSoundSup.get();
    }

    @Override
    public SoundEvent getStepSound() {
        return stepSoundSup.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return placeSoundSup.get();
    }

    @Override
    public SoundEvent getHitSound() {
        return hitSoundSup.get();
    }

    @Override
    public SoundEvent getFallSound() {
        return fallSoundSup.get();
    }
}
