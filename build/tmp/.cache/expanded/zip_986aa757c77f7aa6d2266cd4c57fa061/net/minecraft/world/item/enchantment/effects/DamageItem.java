package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record DamageItem(LevelBasedValue amount) implements EnchantmentEntityEffect {
    public static final MapCodec<DamageItem> CODEC = RecordCodecBuilder.mapCodec(
        p_342623_ -> p_342623_.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(p_344434_ -> p_344434_.amount)).apply(p_342623_, DamageItem::new)
    );

    @Override
    public void apply(ServerLevel p_344645_, int p_342044_, EnchantedItemInUse p_343406_, Entity p_345005_, Vec3 p_343523_) {
        ServerPlayer serverplayer = p_343406_.owner() instanceof ServerPlayer serverplayer1 ? serverplayer1 : null;
        p_343406_.itemStack().hurtAndBreak((int)this.amount.calculate(p_342044_), p_344645_, serverplayer, p_343406_.onBreak());
    }

    @Override
    public MapCodec<DamageItem> codec() {
        return CODEC;
    }
}