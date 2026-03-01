package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259429_ -> p_259429_.point((p_326900_, p_326901_, p_326902_) -> {
                p_326901_.getBrain().updateActivityFromSchedule(p_326900_.getDayTime(), p_326900_.getGameTime());
                return true;
            }));
    }
}