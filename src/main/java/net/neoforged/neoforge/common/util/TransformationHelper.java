package net.neoforged.neoforge.common.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Stub: Transformation helper utilities.
 */
public class TransformationHelper {
    private TransformationHelper() {}

    public static Quaternionf quatFromXYZ(Vector3f xyz, boolean degrees) {
        Quaternionf q = new Quaternionf();
        if (degrees) {
            q.rotateXYZ(
                (float) Math.toRadians(xyz.x()),
                (float) Math.toRadians(xyz.y()),
                (float) Math.toRadians(xyz.z())
            );
        } else {
            q.rotateXYZ(xyz.x(), xyz.y(), xyz.z());
        }
        return q;
    }
}
