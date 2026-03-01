package net.neoforged.neoforge.common.extensions;

/**
 * Stub extension for BlockAndTintGetter.
 */
public interface IBlockAndTintGetterExtension {
    default float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return 1.0f;
    }
}
