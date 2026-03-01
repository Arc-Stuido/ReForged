package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to modify spawning cluster size of a mob.
 */
public class SpawnClusterSizeEvent extends Event {
    private final LivingEntity entity;
    private int clusterSize;

    public SpawnClusterSizeEvent(LivingEntity entity, int clusterSize) {
        this.entity = entity;
        this.clusterSize = clusterSize;
    }

    public LivingEntity getEntity() { return entity; }
    public int getClusterSize() { return clusterSize; }
    public void setClusterSize(int size) { this.clusterSize = size; }
}
