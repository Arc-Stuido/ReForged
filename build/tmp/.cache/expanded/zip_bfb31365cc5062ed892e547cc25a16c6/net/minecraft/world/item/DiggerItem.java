package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;

public class DiggerItem extends TieredItem {
    public DiggerItem(Tier p_204110_, TagKey<Block> p_204111_, Item.Properties p_204112_) {
        super(p_204110_, p_204112_.component(DataComponents.TOOL, p_204110_.createToolProperties(p_204111_)));
    }

    public static ItemAttributeModifiers createAttributes(Tier p_329664_, float p_328988_, float p_329067_) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)(p_328988_ + p_329664_.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)p_329067_, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_345276_, LivingEntity p_342379_, LivingEntity p_342949_) {
        p_345276_.hurtAndBreak(2, p_342949_, EquipmentSlot.MAINHAND);
    }
}