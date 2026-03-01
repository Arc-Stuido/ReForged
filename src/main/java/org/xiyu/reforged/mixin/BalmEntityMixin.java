package org.xiyu.reforged.mixin;

import net.blay09.mods.balm.api.entity.BalmEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class BalmEntityMixin implements BalmEntity {

    @Unique
    private CompoundTag reforged$balmData;

    @Override
    public CompoundTag getFabricBalmData() {
        if (reforged$balmData == null) {
            reforged$balmData = new CompoundTag();
        }
        return reforged$balmData;
    }

    @Override
    public void setFabricBalmData(CompoundTag tag) {
        reforged$balmData = tag == null ? new CompoundTag() : tag;
    }
}
