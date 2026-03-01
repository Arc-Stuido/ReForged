package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class VehicleEntity extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    public VehicleEntity(EntityType<?> p_310168_, Level p_309578_) {
        super(p_310168_, p_309578_);
    }

    @Override
    public boolean hurt(DamageSource p_310829_, float p_310313_) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableTo(p_310829_)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + p_310313_ * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, p_310829_.getEntity());
            boolean flag = p_310829_.getEntity() instanceof Player && ((Player)p_310829_.getEntity()).getAbilities().instabuild;
            if ((flag || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy(p_310829_)) {
                if (flag) {
                    this.discard();
                }
            } else {
                this.destroy(p_310829_);
            }

            return true;
        }
    }

    boolean shouldSourceDestroy(DamageSource p_309621_) {
        return false;
    }

    public void destroy(Item p_313028_) {
        this.kill();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(p_313028_);
            itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            this.spawnAtLocation(itemstack);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332479_) {
        p_332479_.define(DATA_ID_HURT, 0);
        p_332479_.define(DATA_ID_HURTDIR, 1);
        p_332479_.define(DATA_ID_DAMAGE, 0.0F);
    }

    public void setHurtTime(int p_312621_) {
        this.entityData.set(DATA_ID_HURT, p_312621_);
    }

    public void setHurtDir(int p_312074_) {
        this.entityData.set(DATA_ID_HURTDIR, p_312074_);
    }

    public void setDamage(float p_313007_) {
        this.entityData.set(DATA_ID_DAMAGE, p_313007_);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(DamageSource p_312900_) {
        this.destroy(this.getDropItem());
    }

    abstract Item getDropItem();
}