package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

/**
 * Stub: A condition that is always false.
 */
public record FalseCondition() implements ICondition {
    public static final FalseCondition INSTANCE = new FalseCondition();
    public static final MapCodec<FalseCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(ICondition.IContext context) {
        return false;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
