package org.xiyu.reforged.bridge;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;

import java.util.Map;

/**
 * Bridges NeoForge mod command argument types across classloader boundaries.
 *
 * <p>NeoForge mods register argument serializers via {@code ArgumentTypeInfos.registerByClass}
 * inside a {@code DeferredRegister} supplier. When mods load through {@link org.xiyu.reforged.core.NeoModClassLoader},
 * the {@code Class} key stored in {@code BY_CLASS} may differ from the {@code Class} object seen at
 * runtime on the integrated server, causing {@code Unrecognized argument type} during player login.</p>
 */
public final class ArgumentTypeBridge {

    private ArgumentTypeBridge() {
    }

    @SuppressWarnings("unchecked")
    public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> resolve(
            A argument,
            Map<Class<?>, ArgumentTypeInfo<?, ?>> byClass) {
        ArgumentTypeInfo<?, ?> direct = byClass.get(argument.getClass());
        if (direct != null) {
            return (ArgumentTypeInfo<A, ?>) direct;
        }

        String runtimeName = argument.getClass().getName();
        for (Map.Entry<Class<?>, ArgumentTypeInfo<?, ?>> entry : byClass.entrySet()) {
            if (entry.getKey().getName().equals(runtimeName)) {
                return (ArgumentTypeInfo<A, ?>) entry.getValue();
            }
        }
        return null;
    }

    public static boolean isRecognized(Class<?> clazz, Map<Class<?>, ArgumentTypeInfo<?, ?>> byClass) {
        if (byClass.containsKey(clazz)) {
            return true;
        }
        String name = clazz.getName();
        for (Class<?> key : byClass.keySet()) {
            if (key.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
