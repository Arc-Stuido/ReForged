package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;

/**
 * Stub for NeoForge's IClientBlockExtensions.
 * Allows blocks to customize client-side rendering behavior.
 */
public interface IClientBlockExtensions {
    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {};

    static IClientBlockExtensions of(BlockState state) {
        return of(state.getBlock());
    }

    static IClientBlockExtensions of(Block block) {
        // In NeoForge, this uses ClientExtensionsManager. Simplified to return DEFAULT.
        return DEFAULT;
    }

    default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        return false;
    }

    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return !state.shouldSpawnTerrainParticles();
    }

    default boolean playBreakSound(BlockState state, Level level, BlockPos pos) {
        return false;
    }

    default boolean areBreakingParticlesTinted(BlockState state, ClientLevel level, BlockPos pos) {
        return true;
    }
}
