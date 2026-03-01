package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

/** Proxy: NeoForge's ICondition interface for conditional loading */
public interface ICondition {
    boolean test(IContext context);

    default MapCodec<? extends ICondition> codec() {
        return MapCodec.unit(this);
    }

    public interface IContext {
    }
}
