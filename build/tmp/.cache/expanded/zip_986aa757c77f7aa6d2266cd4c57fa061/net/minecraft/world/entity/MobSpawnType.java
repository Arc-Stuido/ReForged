package net.minecraft.world.entity;

public enum MobSpawnType {
    NATURAL,
    CHUNK_GENERATION,
    SPAWNER,
    STRUCTURE,
    BREEDING,
    MOB_SUMMONED,
    JOCKEY,
    EVENT,
    CONVERSION,
    REINFORCEMENT,
    TRIGGERED,
    BUCKET,
    SPAWN_EGG,
    COMMAND,
    DISPENSER,
    PATROL,
    TRIAL_SPAWNER;

    public static boolean isSpawner(MobSpawnType p_311852_) {
        return p_311852_ == SPAWNER || p_311852_ == TRIAL_SPAWNER;
    }

    public static boolean ignoresLightRequirements(MobSpawnType p_310571_) {
        return p_310571_ == TRIAL_SPAWNER;
    }
}