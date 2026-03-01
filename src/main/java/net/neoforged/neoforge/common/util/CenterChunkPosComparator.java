package net.neoforged.neoforge.common.util;

import net.minecraft.world.level.ChunkPos;
import java.util.Comparator;

/**
 * Comparator that sorts ChunkPos by distance from center.
 */
public class CenterChunkPosComparator implements Comparator<ChunkPos> {
    private final ChunkPos center;

    public CenterChunkPosComparator(ChunkPos center) {
        this.center = center;
    }

    @Override
    public int compare(ChunkPos a, ChunkPos b) {
        int distA = distSq(a);
        int distB = distSq(b);
        return Integer.compare(distA, distB);
    }

    private int distSq(ChunkPos pos) {
        int dx = pos.x - center.x;
        int dz = pos.z - center.z;
        return dx * dx + dz * dz;
    }
}
