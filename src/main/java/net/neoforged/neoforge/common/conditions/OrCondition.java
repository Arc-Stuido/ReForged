package net.neoforged.neoforge.common.conditions;

import java.util.List;

/**
 * Stub: OR condition - at least one sub-condition must be true.
 */
public record OrCondition(List<ICondition> conditions) implements ICondition {
    @Override
    public boolean test(ICondition.IContext context) {
        for (ICondition c : conditions) {
            if (c.test(context)) return true;
        }
        return false;
    }
}
