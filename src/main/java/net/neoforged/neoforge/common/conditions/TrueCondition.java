package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

/**
 * Stub: A condition that is always true.
 */
public record TrueCondition() implements ICondition {
    public static final TrueCondition INSTANCE = new TrueCondition();
    public static final MapCodec<TrueCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(ICondition.IContext context) {
        return true;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
