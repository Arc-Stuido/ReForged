package net.neoforged.neoforge.common.conditions;

import net.minecraft.resources.ResourceLocation;

/**
 * Stub: Condition that checks if an item exists in the registry.
 */
public record ItemExistsCondition(ResourceLocation item) implements ICondition {
    @Override
    public boolean test(ICondition.IContext context) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(item);
    }
}
