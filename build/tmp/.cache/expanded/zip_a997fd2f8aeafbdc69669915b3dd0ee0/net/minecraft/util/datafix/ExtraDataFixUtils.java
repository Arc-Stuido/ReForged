package net.minecraft.util.datafix;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ExtraDataFixUtils {
    public static Dynamic<?> fixBlockPos(Dynamic<?> p_330445_) {
        Optional<Number> optional = p_330445_.get("X").asNumber().result();
        Optional<Number> optional1 = p_330445_.get("Y").asNumber().result();
        Optional<Number> optional2 = p_330445_.get("Z").asNumber().result();
        return !optional.isEmpty() && !optional1.isEmpty() && !optional2.isEmpty()
            ? p_330445_.createIntList(IntStream.of(optional.get().intValue(), optional1.get().intValue(), optional2.get().intValue()))
            : p_330445_;
    }

    public static <T, R> Typed<R> cast(Type<R> p_332791_, Typed<T> p_329826_) {
        return new Typed<>(p_332791_, p_329826_.getOps(), (R)p_329826_.getValue());
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... p_343226_) {
        return p_344666_ -> {
            for (Function<Typed<?>, Typed<?>> function : p_343226_) {
                p_344666_ = function.apply(p_344666_);
            }

            return p_344666_;
        };
    }
}