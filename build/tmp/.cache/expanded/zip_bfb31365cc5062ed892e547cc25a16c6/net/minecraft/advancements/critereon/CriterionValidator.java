package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
    private final ProblemReporter reporter;
    private final HolderGetter.Provider lootData;

    public CriterionValidator(ProblemReporter p_311865_, HolderGetter.Provider p_329172_) {
        this.reporter = p_311865_;
        this.lootData = p_329172_;
    }

    public void validateEntity(Optional<ContextAwarePredicate> p_311203_, String p_309703_) {
        p_311203_.ifPresent(p_312443_ -> this.validateEntity(p_312443_, p_309703_));
    }

    public void validateEntities(List<ContextAwarePredicate> p_310532_, String p_310219_) {
        this.validate(p_310532_, LootContextParamSets.ADVANCEMENT_ENTITY, p_310219_);
    }

    public void validateEntity(ContextAwarePredicate p_310373_, String p_309633_) {
        this.validate(p_310373_, LootContextParamSets.ADVANCEMENT_ENTITY, p_309633_);
    }

    public void validate(ContextAwarePredicate p_311627_, LootContextParamSet p_312598_, String p_312977_) {
        p_311627_.validate(new ValidationContext(this.reporter.forChild(p_312977_), p_312598_, this.lootData));
    }

    public void validate(List<ContextAwarePredicate> p_309439_, LootContextParamSet p_311765_, String p_309737_) {
        for (int i = 0; i < p_309439_.size(); i++) {
            ContextAwarePredicate contextawarepredicate = p_309439_.get(i);
            contextawarepredicate.validate(new ValidationContext(this.reporter.forChild(p_309737_ + "[" + i + "]"), p_311765_, this.lootData));
        }
    }
}