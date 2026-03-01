package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public interface DispenseItemBehavior {
    Logger LOGGER = LogUtils.getLogger();
    DispenseItemBehavior NOOP = (p_123400_, p_123401_) -> p_123401_;

    ItemStack dispense(BlockSource p_123403_, ItemStack p_123404_);

    static void bootStrap() {
        DispenserBlock.registerProjectileBehavior(Items.ARROW);
        DispenserBlock.registerProjectileBehavior(Items.TIPPED_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.SPECTRAL_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.EGG);
        DispenserBlock.registerProjectileBehavior(Items.SNOWBALL);
        DispenserBlock.registerProjectileBehavior(Items.EXPERIENCE_BOTTLE);
        DispenserBlock.registerProjectileBehavior(Items.SPLASH_POTION);
        DispenserBlock.registerProjectileBehavior(Items.LINGERING_POTION);
        DispenserBlock.registerProjectileBehavior(Items.FIREWORK_ROCKET);
        DispenserBlock.registerProjectileBehavior(Items.FIRE_CHARGE);
        DispenserBlock.registerProjectileBehavior(Items.WIND_CHARGE);
        DefaultDispenseItemBehavior defaultdispenseitembehavior = new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource p_327707_, ItemStack p_329825_) {
                Direction direction = p_327707_.state().getValue(DispenserBlock.FACING);
                EntityType<?> entitytype = ((SpawnEggItem)p_329825_.getItem()).getType(p_329825_);

                try {
                    entitytype.spawn(
                        p_327707_.level(),
                        p_329825_,
                        null,
                        p_327707_.pos().relative(direction),
                        MobSpawnType.DISPENSER,
                        direction != Direction.UP,
                        false
                    );
                } catch (Exception exception) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", p_327707_.pos(), exception);
                    return ItemStack.EMPTY;
                }

                p_329825_.shrink(1);
                p_327707_.level().gameEvent(null, GameEvent.ENTITY_PLACE, p_327707_.pos());
                return p_329825_;
            }
        };

        for (SpawnEggItem spawneggitem : SpawnEggItem.eggs()) {
            DispenserBlock.registerBehavior(spawneggitem, defaultdispenseitembehavior);
        }

        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource p_334267_, ItemStack p_328475_) {
                Direction direction = p_334267_.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = p_334267_.pos().relative(direction);
                ServerLevel serverlevel = p_334267_.level();
                Consumer<ArmorStand> consumer = EntityType.appendDefaultStackConfig(p_341004_ -> p_341004_.setYRot(direction.toYRot()), serverlevel, p_328475_, null);
                ArmorStand armorstand = EntityType.ARMOR_STAND.spawn(serverlevel, consumer, blockpos, MobSpawnType.DISPENSER, false, false);
                if (armorstand != null) {
                    p_328475_.shrink(1);
                }

                return p_328475_;
            }
        });
        DispenserBlock.registerBehavior(
            Items.SADDLE,
            new OptionalDispenseItemBehavior() {
                @Override
                public ItemStack execute(BlockSource p_328289_, ItemStack p_334031_) {
                    BlockPos blockpos = p_328289_.pos().relative(p_328289_.state().getValue(DispenserBlock.FACING));
                    List<LivingEntity> list = p_328289_.level()
                        .getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(blockpos),
                            p_329808_ -> !(p_329808_ instanceof Saddleable saddleable) ? false : !saddleable.isSaddled() && saddleable.isSaddleable()
                        );
                    if (!list.isEmpty()) {
                        ((Saddleable)list.get(0)).equipSaddle(p_334031_.split(1), SoundSource.BLOCKS);
                        this.setSuccess(true);
                        return p_334031_;
                    } else {
                        return super.execute(p_328289_, p_334031_);
                    }
                }
            }
        );
        DefaultDispenseItemBehavior defaultdispenseitembehavior1 = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_334868_, ItemStack p_334276_) {
                BlockPos blockpos = p_334868_.pos().relative(p_334868_.state().getValue(DispenserBlock.FACING));

                for (AbstractHorse abstracthorse : p_334868_.level()
                    .getEntitiesOfClass(AbstractHorse.class, new AABB(blockpos), p_341005_ -> p_341005_.isAlive() && p_341005_.canUseSlot(EquipmentSlot.BODY))) {
                    if (abstracthorse.isBodyArmorItem(p_334276_) && !abstracthorse.isWearingBodyArmor() && abstracthorse.isTamed()) {
                        abstracthorse.setBodyArmorItem(p_334276_.split(1));
                        this.setSuccess(true);
                        return p_334276_;
                    }
                }

                return super.execute(p_334868_, p_334276_);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.RED_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(
            Items.CHEST,
            new OptionalDispenseItemBehavior() {
                @Override
                public ItemStack execute(BlockSource p_335842_, ItemStack p_335219_) {
                    BlockPos blockpos = p_335842_.pos().relative(p_335842_.state().getValue(DispenserBlock.FACING));

                    for (AbstractChestedHorse abstractchestedhorse : p_335842_.level()
                        .getEntitiesOfClass(AbstractChestedHorse.class, new AABB(blockpos), p_341006_ -> p_341006_.isAlive() && !p_341006_.hasChest())) {
                        if (abstractchestedhorse.isTamed() && abstractchestedhorse.getSlot(499).set(p_335219_)) {
                            p_335219_.shrink(1);
                            this.setSuccess(true);
                            return p_335219_;
                        }
                    }

                    return super.execute(p_335842_, p_335219_);
                }
            }
        );
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
        DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY));
        DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE));
        DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO));
        DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK, true));
        DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE, true));
        DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH, true));
        DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE, true));
        DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK, true));
        DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA, true));
        DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY, true));
        DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE, true));
        DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO, true));
        DispenseItemBehavior dispenseitembehavior1 = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource p_333645_, ItemStack p_333855_) {
                DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem)p_333855_.getItem();
                BlockPos blockpos = p_333645_.pos().relative(p_333645_.state().getValue(DispenserBlock.FACING));
                Level level = p_333645_.level();
                if (dispensiblecontaineritem.emptyContents(null, level, blockpos, null, p_333855_)) {
                    dispensiblecontaineritem.checkExtraContent(null, level, p_333855_, blockpos);
                    return this.consumeWithRemainder(p_333645_, p_333855_, new ItemStack(Items.BUCKET));
                } else {
                    return this.defaultDispenseItemBehavior.dispense(p_333645_, p_333855_);
                }
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource p_332842_, ItemStack p_335191_) {
                LevelAccessor levelaccessor = p_332842_.level();
                BlockPos blockpos = p_332842_.pos().relative(p_332842_.state().getValue(DispenserBlock.FACING));
                BlockState blockstate = levelaccessor.getBlockState(blockpos);
                if (blockstate.getBlock() instanceof BucketPickup bucketpickup) {
                    ItemStack itemstack = bucketpickup.pickupBlock(null, levelaccessor, blockpos, blockstate);
                    if (itemstack.isEmpty()) {
                        return super.execute(p_332842_, p_335191_);
                    } else {
                        levelaccessor.gameEvent(null, GameEvent.FLUID_PICKUP, blockpos);
                        Item item = itemstack.getItem();
                        return this.consumeWithRemainder(p_332842_, p_335191_, new ItemStack(item));
                    }
                } else {
                    return super.execute(p_332842_, p_335191_);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_333039_, ItemStack p_335778_) {
                ServerLevel serverlevel = p_333039_.level();
                this.setSuccess(true);
                Direction direction = p_333039_.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = p_333039_.pos().relative(direction);
                BlockState blockstate = serverlevel.getBlockState(blockpos);
                if (BaseFireBlock.canBePlacedAt(serverlevel, blockpos, direction)) {
                    serverlevel.setBlockAndUpdate(blockpos, BaseFireBlock.getState(serverlevel, blockpos));
                    serverlevel.gameEvent(null, GameEvent.BLOCK_PLACE, blockpos);
                } else if (CampfireBlock.canLight(blockstate) || CandleBlock.canLight(blockstate) || CandleCakeBlock.canLight(blockstate)) {
                    serverlevel.setBlockAndUpdate(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                    serverlevel.gameEvent(null, GameEvent.BLOCK_CHANGE, blockpos);
                } else if (blockstate.isFlammable(serverlevel, blockpos, p_333039_.state().getValue(DispenserBlock.FACING).getOpposite())) {
                    blockstate.onCaughtFire(serverlevel, blockpos, p_333039_.state().getValue(DispenserBlock.FACING).getOpposite(), null);
                    if (blockstate.getBlock() instanceof TntBlock) {
                        serverlevel.removeBlock(blockpos, false);
                    }
                } else {
                    this.setSuccess(false);
                }

                if (this.isSuccess()) {
                    p_335778_.hurtAndBreak(1, serverlevel, null, p_341007_ -> {
                    });
                }

                return p_335778_;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_123523_, ItemStack p_123524_) {
                this.setSuccess(true);
                Level level = p_123523_.level();
                BlockPos blockpos = p_123523_.pos().relative(p_123523_.state().getValue(DispenserBlock.FACING));
                if (!BoneMealItem.growCrop(p_123524_, level, blockpos) && !BoneMealItem.growWaterPlant(p_123524_, level, blockpos, null)) {
                    this.setSuccess(false);
                } else if (!level.isClientSide) {
                    level.levelEvent(1505, blockpos, 15);
                }

                return p_123524_;
            }
        });
        DispenserBlock.registerBehavior(
            Blocks.TNT,
            new DefaultDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource p_123461_, ItemStack p_123462_) {
                    Level level = p_123461_.level();
                    BlockPos blockpos = p_123461_.pos().relative(p_123461_.state().getValue(DispenserBlock.FACING));
                    PrimedTnt primedtnt = new PrimedTnt(
                        level, (double)blockpos.getX() + 0.5, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5, null
                    );
                    level.addFreshEntity(primedtnt);
                    level.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                    p_123462_.shrink(1);
                    return p_123462_;
                }
            }
        );
        DispenseItemBehavior dispenseitembehavior = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_123529_, ItemStack p_123530_) {
                this.setSuccess(ArmorItem.dispenseArmor(p_123529_, p_123530_));
                return p_123530_;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.PIGLIN_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_123535_, ItemStack p_123536_) {
                Level level = p_123535_.level();
                Direction direction = p_123535_.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = p_123535_.pos().relative(direction);
                if (level.isEmptyBlock(blockpos) && WitherSkullBlock.canSpawnMob(level, blockpos, p_123536_)) {
                    level.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(RotationSegment.convertToSegment(direction))), 3);
                    level.gameEvent(null, GameEvent.BLOCK_PLACE, blockpos);
                    BlockEntity blockentity = level.getBlockEntity(blockpos);
                    if (blockentity instanceof SkullBlockEntity) {
                        WitherSkullBlock.checkSpawn(level, blockpos, (SkullBlockEntity)blockentity);
                    }

                    p_123536_.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(p_123535_, p_123536_));
                }

                return p_123536_;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_123541_, ItemStack p_123542_) {
                Level level = p_123541_.level();
                BlockPos blockpos = p_123541_.pos().relative(p_123541_.state().getValue(DispenserBlock.FACING));
                CarvedPumpkinBlock carvedpumpkinblock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
                if (level.isEmptyBlock(blockpos) && carvedpumpkinblock.canSpawnGolem(level, blockpos)) {
                    if (!level.isClientSide) {
                        level.setBlock(blockpos, carvedpumpkinblock.defaultBlockState(), 3);
                        level.gameEvent(null, GameEvent.BLOCK_PLACE, blockpos);
                    }

                    p_123542_.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(p_123541_, p_123542_));
                }

                return p_123542_;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

        for (DyeColor dyecolor : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(dyecolor).asItem(), new ShulkerBoxDispenseBehavior());
        }

        DispenserBlock.registerBehavior(
            Items.GLASS_BOTTLE.asItem(),
            new OptionalDispenseItemBehavior() {
                private ItemStack takeLiquid(BlockSource p_330210_, ItemStack p_334816_, ItemStack p_330695_) {
                    p_330210_.level().gameEvent(null, GameEvent.FLUID_PICKUP, p_330210_.pos());
                    return this.consumeWithRemainder(p_330210_, p_334816_, p_330695_);
                }

                @Override
                public ItemStack execute(BlockSource p_123547_, ItemStack p_123548_) {
                    this.setSuccess(false);
                    ServerLevel serverlevel = p_123547_.level();
                    BlockPos blockpos = p_123547_.pos().relative(p_123547_.state().getValue(DispenserBlock.FACING));
                    BlockState blockstate = serverlevel.getBlockState(blockpos);
                    if (blockstate.is(
                            BlockTags.BEEHIVES, p_333210_ -> p_333210_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_333210_.getBlock() instanceof BeehiveBlock
                        )
                        && blockstate.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                        ((BeehiveBlock)blockstate.getBlock())
                            .releaseBeesAndResetHoneyLevel(serverlevel, blockstate, blockpos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                        this.setSuccess(true);
                        return this.takeLiquid(p_123547_, p_123548_, new ItemStack(Items.HONEY_BOTTLE));
                    } else if (serverlevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                        this.setSuccess(true);
                        return this.takeLiquid(p_123547_, p_123548_, PotionContents.createItemStack(Items.POTION, Potions.WATER));
                    } else {
                        return super.execute(p_123547_, p_123548_);
                    }
                }
            }
        );
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource p_123556_, ItemStack p_123557_) {
                Direction direction = p_123556_.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = p_123556_.pos().relative(direction);
                Level level = p_123556_.level();
                BlockState blockstate = level.getBlockState(blockpos);
                this.setSuccess(true);
                if (blockstate.is(Blocks.RESPAWN_ANCHOR)) {
                    if (blockstate.getValue(RespawnAnchorBlock.CHARGE) != 4) {
                        RespawnAnchorBlock.charge(null, level, blockpos, blockstate);
                        p_123557_.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return p_123557_;
                } else {
                    return super.execute(p_123556_, p_123557_);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
        DispenserBlock.registerBehavior(Items.BRUSH.asItem(), new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource p_123561_, ItemStack p_123562_) {
                ServerLevel serverlevel = p_123561_.level();
                BlockPos blockpos = p_123561_.pos().relative(p_123561_.state().getValue(DispenserBlock.FACING));
                List<Armadillo> list = serverlevel.getEntitiesOfClass(Armadillo.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS);
                if (list.isEmpty()) {
                    this.setSuccess(false);
                    return p_123562_;
                } else {
                    for (Armadillo armadillo : list) {
                        if (armadillo.brushOffScute()) {
                            p_123562_.hurtAndBreak(16, serverlevel, null, p_341002_ -> {
                            });
                            return p_123562_;
                        }
                    }

                    this.setSuccess(false);
                    return p_123562_;
                }
            }
        });
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new OptionalDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource p_123566_, ItemStack p_123567_) {
                BlockPos blockpos = p_123566_.pos().relative(p_123566_.state().getValue(DispenserBlock.FACING));
                Level level = p_123566_.level();
                BlockState blockstate = level.getBlockState(blockpos);
                Optional<BlockState> optional = HoneycombItem.getWaxed(blockstate);
                if (optional.isPresent()) {
                    level.setBlockAndUpdate(blockpos, optional.get());
                    level.levelEvent(3003, blockpos, 0);
                    p_123567_.shrink(1);
                    this.setSuccess(true);
                    return p_123567_;
                } else {
                    return super.execute(p_123566_, p_123567_);
                }
            }
        });
        DispenserBlock.registerBehavior(
            Items.POTION,
            new DefaultDispenseItemBehavior() {
                private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

                @Override
                public ItemStack execute(BlockSource p_123412_, ItemStack p_123413_) {
                    PotionContents potioncontents = p_123413_.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (!potioncontents.is(Potions.WATER)) {
                        return this.defaultDispenseItemBehavior.dispense(p_123412_, p_123413_);
                    } else {
                        ServerLevel serverlevel = p_123412_.level();
                        BlockPos blockpos = p_123412_.pos();
                        BlockPos blockpos1 = p_123412_.pos().relative(p_123412_.state().getValue(DispenserBlock.FACING));
                        if (!serverlevel.getBlockState(blockpos1).is(BlockTags.CONVERTABLE_TO_MUD)) {
                            return this.defaultDispenseItemBehavior.dispense(p_123412_, p_123413_);
                        } else {
                            if (!serverlevel.isClientSide) {
                                for (int i = 0; i < 5; i++) {
                                    serverlevel.sendParticles(
                                        ParticleTypes.SPLASH,
                                        (double)blockpos.getX() + serverlevel.random.nextDouble(),
                                        (double)(blockpos.getY() + 1),
                                        (double)blockpos.getZ() + serverlevel.random.nextDouble(),
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0
                                    );
                                }
                            }

                            serverlevel.playSound(null, blockpos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            serverlevel.gameEvent(null, GameEvent.FLUID_PLACE, blockpos);
                            serverlevel.setBlockAndUpdate(blockpos1, Blocks.MUD.defaultBlockState());
                            return this.consumeWithRemainder(p_123412_, p_123413_, new ItemStack(Items.GLASS_BOTTLE));
                        }
                    }
                }
            }
        );
    }
}
