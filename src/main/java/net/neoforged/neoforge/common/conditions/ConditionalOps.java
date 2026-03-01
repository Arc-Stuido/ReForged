package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.DynamicOps;

/**
 * Stub: Conditional DynamicOps wrapper for conditional JSON loading.
 */
public class ConditionalOps {
    private ConditionalOps() {}

    public static <T> DynamicOps<T> create(DynamicOps<T> ops, ICondition.IContext context) {
        return ops; // Passthrough in stub
    }
}
