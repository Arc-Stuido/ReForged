package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) {
    public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> p_345149_, LootContextParamSet p_344121_) {
        return RecordCodecBuilder.create(
            p_343058_ -> p_343058_.group(
                        EnchantmentTarget.CODEC.fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted),
                        EnchantmentTarget.CODEC.fieldOf("affected").forGetter(TargetedConditionalEffect::affected),
                        p_345149_.fieldOf("effect").forGetter((Function<TargetedConditionalEffect<S>, S>)(TargetedConditionalEffect::effect)),
                        ConditionalEffect.conditionCodec(p_344121_).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
                    )
                    .apply(
                        p_343058_,
                        (Function4<EnchantmentTarget, EnchantmentTarget, S, Optional<LootItemCondition>, TargetedConditionalEffect<S>>)(TargetedConditionalEffect::new)
                    )
        );
    }

    public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> p_343050_, LootContextParamSet p_343380_) {
        return RecordCodecBuilder.create(
            p_342227_ -> p_342227_.group(
                        EnchantmentTarget.CODEC
                            .validate(
                                p_342627_ -> p_342627_ != EnchantmentTarget.DAMAGING_ENTITY
                                        ? DataResult.success(p_342627_)
                                        : DataResult.error(() -> "enchanted must be attacker or victim")
                            )
                            .fieldOf("enchanted")
                            .forGetter(TargetedConditionalEffect::enchanted),
                        p_343050_.fieldOf("effect").forGetter((Function<TargetedConditionalEffect<S>, S>)(TargetedConditionalEffect::effect)),
                        ConditionalEffect.conditionCodec(p_343380_).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
                    )
                    .apply(
                        p_342227_,
                        (p_345323_, p_343604_, p_343198_) -> new TargetedConditionalEffect<>(p_345323_, EnchantmentTarget.VICTIM, p_343604_, p_343198_)
                    )
        );
    }

    public boolean matches(LootContext p_343867_) {
        return this.requirements.isEmpty() ? true : this.requirements.get().test(p_343867_);
    }
}