package net.neoforged.neoforge.common.conditions;

/**
 * Stub: Interface for building conditions in data providers.
 */
public interface IConditionBuilder {

    default ICondition and(ICondition... conditions) {
        return new AndCondition(java.util.List.of(conditions));
    }

    default ICondition or(ICondition... conditions) {
        return new OrCondition(java.util.List.of(conditions));
    }

    default ICondition itemExists(String namespace, String path) {
        return new ItemExistsCondition(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
