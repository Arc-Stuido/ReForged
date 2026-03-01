package net.neoforged.neoforge.common.conditions;

import java.util.List;

/**
 * Stub: AND condition - all sub-conditions must be true.
 */
public record AndCondition(List<ICondition> conditions) implements ICondition {
    @Override
    public boolean test(ICondition.IContext context) {
        for (ICondition c : conditions) {
            if (!c.test(context)) return false;
        }
        return true;
    }
}
