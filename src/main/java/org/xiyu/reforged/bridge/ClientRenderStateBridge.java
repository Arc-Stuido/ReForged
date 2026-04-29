package org.xiyu.reforged.bridge;

import java.lang.reflect.Field;

import org.joml.Matrix4f;
import org.xiyu.reforged.core.NeoForgeModLoader;

/**
 * Shares render-frame state with NeoForge mods whose own mixins cannot be
 * applied from the child mod classloader.
 */
public final class ClientRenderStateBridge {
    private static Field superbWarfareModelViewMatrix;
    private static Field superbWarfareProjectionMatrix;
    private static long retryLookupAfterNanos;

    private ClientRenderStateBridge() {
    }

    public static void captureLevelMatrices(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (modelViewMatrix == null || projectionMatrix == null) {
            return;
        }

        try {
            if (!ensureSuperbWarfareVectorUtil()) {
                return;
            }

            superbWarfareModelViewMatrix.set(null, new Matrix4f(modelViewMatrix));
            superbWarfareProjectionMatrix.set(null, new Matrix4f(projectionMatrix));
        } catch (Throwable ignored) {
            retryLookupAfterNanos = System.nanoTime() + 5_000_000_000L;
        }
    }

    private static boolean ensureSuperbWarfareVectorUtil() throws ReflectiveOperationException {
        if (superbWarfareModelViewMatrix != null && superbWarfareProjectionMatrix != null) {
            return true;
        }

        long now = System.nanoTime();
        if (retryLookupAfterNanos > now) {
            return false;
        }

        ClassLoader loader = NeoForgeModLoader.getNeoModClassLoader();
        if (loader == null) {
            retryLookupAfterNanos = now + 5_000_000_000L;
            return false;
        }

        Class<?> vectorUtil = loader.loadClass("com.atsuishio.superbwarfare.tools.VectorUtil");
        superbWarfareModelViewMatrix = vectorUtil.getField("modelViewMatrix");
        superbWarfareProjectionMatrix = vectorUtil.getField("projectionMatrix");
        return true;
    }
}
