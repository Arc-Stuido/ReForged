package net.neoforged.neoforge.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Stub: Interface for custom arm pose transformations.
 */
@FunctionalInterface
public interface IArmPoseTransformer {
    void applyTransform(HumanoidModel<?> model, LivingEntity entity, HumanoidModel.ArmPose armPose);
}
