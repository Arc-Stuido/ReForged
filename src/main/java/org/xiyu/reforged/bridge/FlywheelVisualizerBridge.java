package org.xiyu.reforged.bridge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Bridge for Flywheel's visualizer registry.
 *
 * <p>NeoForge's Flywheel uses Mixins to make {@code EntityType} implement
 * {@code EntityTypeExtension} and {@code BlockEntityType} implement
 * {@code BlockEntityTypeExtension}. These Mixins cannot be applied in the
 * Forge environment. Instead, we intercept the CHECKCAST + INVOKEINTERFACE
 * pattern in {@code VisualizerRegistryImpl} (via {@code MethodCallRedirector})
 * and store visualizers in side maps here.</p>
 */
public final class FlywheelVisualizerBridge {

    private FlywheelVisualizerBridge() {}

    // Maps keyed by identity (EntityType / BlockEntityType) → visualizer object
    private static final Map<Object, Object> ENTITY_VISUALIZERS = new ConcurrentHashMap<>();
    private static final Map<Object, Object> BLOCK_ENTITY_VISUALIZERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> ENTITY_VISUAL_FACTORY_METHODS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> PREDICATE_TEST_METHODS = new ConcurrentHashMap<>();
    private static int visualCreateFailureCount;
    private static int predicateFailureCount;

    // ── Entity visualizers ─────────────────────────────────────────────

    public static void setEntityVisualizer(Object entityType, Object visualizer) {
        if (entityType != null && visualizer != null) {
            ENTITY_VISUALIZERS.put(entityType, visualizer);
        }
    }

    public static Object getEntityVisualizer(Object entityType) {
        return ENTITY_VISUALIZERS.get(entityType);
    }

    // ── Block entity visualizers ───────────────────────────────────────

    public static Object createEntityVisualSafe(Object factory, Object context, Object entity, float partialTick) {
        if (factory == null) return null;
        try {
            Method method = ENTITY_VISUAL_FACTORY_METHODS.computeIfAbsent(factory.getClass(),
                    FlywheelVisualizerBridge::findEntityVisualFactoryMethod);
            if (method == null) return null;
            return method.invoke(factory, context, entity, partialTick);
        } catch (InvocationTargetException e) {
            if (visualCreateFailureCount++ < 5) {
                System.err.println("[ReForged] Suppressed incompatible Flywheel entity visual: "
                        + e.getTargetException());
            }
            return null;
        } catch (Throwable t) {
            if (visualCreateFailureCount++ < 5) {
                System.err.println("[ReForged] Suppressed Flywheel entity visual failure: " + t);
            }
            return null;
        }
    }

    public static Object createEntityVisualSafe(Object factory, Object context,
                                                net.minecraft.world.entity.Entity entity, float partialTick) {
        return createEntityVisualSafe(factory, context, (Object) entity, partialTick);
    }

    public static boolean testEntityPredicateSafe(Object predicate, Object entity) {
        if (predicate == null) return false;
        try {
            Method method = PREDICATE_TEST_METHODS.computeIfAbsent(predicate.getClass(),
                    FlywheelVisualizerBridge::findPredicateTestMethod);
            return method != null && Boolean.TRUE.equals(method.invoke(predicate, entity));
        } catch (InvocationTargetException e) {
            if (predicateFailureCount++ < 5) {
                System.err.println("[ReForged] Suppressed incompatible Flywheel entity predicate: "
                        + e.getTargetException());
            }
            return false;
        } catch (Throwable t) {
            if (predicateFailureCount++ < 5) {
                System.err.println("[ReForged] Suppressed Flywheel entity predicate failure: " + t);
            }
            return false;
        }
    }

    private static Method findEntityVisualFactoryMethod(Class<?> factoryClass) {
        for (Method method : factoryClass.getMethods()) {
            if ("create".equals(method.getName()) && method.getParameterCount() == 3) {
                method.trySetAccessible();
                return method;
            }
        }
        return null;
    }

    private static Method findPredicateTestMethod(Class<?> predicateClass) {
        for (Method method : predicateClass.getMethods()) {
            if ("test".equals(method.getName()) && method.getParameterCount() == 1) {
                method.trySetAccessible();
                return method;
            }
        }
        return null;
    }

    public static void setBlockEntityVisualizer(Object blockEntityType, Object visualizer) {
        if (blockEntityType != null && visualizer != null) {
            BLOCK_ENTITY_VISUALIZERS.put(blockEntityType, visualizer);
        }
    }

    public static Object getBlockEntityVisualizer(Object blockEntityType) {
        return BLOCK_ENTITY_VISUALIZERS.get(blockEntityType);
    }
}
