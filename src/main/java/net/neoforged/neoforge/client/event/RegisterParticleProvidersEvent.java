package net.neoforged.neoforge.client.event;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/**
 * Wrapper around Forge's {@link net.minecraftforge.client.event.RegisterParticleProvidersEvent}.
 */
public class RegisterParticleProvidersEvent {
    private final net.minecraftforge.client.event.RegisterParticleProvidersEvent delegate;

    public RegisterParticleProvidersEvent(net.minecraftforge.client.event.RegisterParticleProvidersEvent delegate) {
        this.delegate = delegate;
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type,
                                                               ParticleEngine.SpriteParticleRegistration<T> registration) {
        delegate.registerSpriteSet(type, registration);
    }

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type,
                                                             ParticleProvider<T> provider) {
        delegate.registerSpecial(type, provider);
    }
}
