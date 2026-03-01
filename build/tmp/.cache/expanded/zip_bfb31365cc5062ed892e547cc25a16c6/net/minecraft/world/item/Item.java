package net.minecraft.world.item;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike, net.minecraftforge.common.extensions.IForgeItem {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<Item>> STREAM_CODEC =
        net.minecraft.network.codec.ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.ITEM);
    public static final Map<Block, Item> BY_BLOCK = net.minecraftforge.registries.GameData.ItemCallbacks.getBlockItemMap();
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
    public static final int DEFAULT_MAX_STACK_SIZE = 64;
    public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
    private final DataComponentMap components;
    @Nullable
    private final Item craftingRemainingItem;
    @Nullable
    private String descriptionId;
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item p_41394_) {
        return p_41394_ == null ? 0 : BuiltInRegistries.ITEM.getId(p_41394_);
    }

    public static Item byId(int p_41446_) {
        return BuiltInRegistries.ITEM.byId(p_41446_);
    }

    @Deprecated
    public static Item byBlock(Block p_41440_) {
        return BY_BLOCK.getOrDefault(p_41440_, Items.AIR);
    }

    public Item(Item.Properties p_41383_) {
        this.components = p_41383_.buildAndValidateComponents();
        this.craftingRemainingItem = p_41383_.craftingRemainingItem;
        this.requiredFeatures = p_41383_.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();
            if (!s.endsWith("Item")) {
                LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }
        initClient();
    }

    @Deprecated
    public Holder.Reference<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    @Nullable
    private DataComponentMap builtComponents = null;

    public DataComponentMap components() {
        if (builtComponents == null) {
            builtComponents = net.minecraftforge.common.ForgeHooks.gatherItemComponents(this, components);
        }

        return builtComponents;
    }

    public int getDefaultMaxStackSize() {
        return builtComponents == null ? this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) : this.builtComponents.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
    }

    /** @deprecated Forge: {@link net.minecraftforge.common.extensions.IForgeItem#onDestroyed(ItemEntity, DamageSource) Use damage source sensitive version} */
    public void onDestroyed(ItemEntity p_150887_) {
    }

    public void verifyComponentsAfterLoad(ItemStack p_336236_) {
    }

    public boolean canAttackBlock(BlockState p_41441_, Level p_41442_, BlockPos p_41443_, Player p_41444_) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public InteractionResult useOn(UseOnContext p_41427_) {
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack p_41425_, BlockState p_41426_) {
        Tool tool = p_41425_.get(DataComponents.TOOL);
        return tool != null ? tool.getMiningSpeed(p_41426_) : 1.0F;
    }

    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        ItemStack itemstack = p_41433_.getItemInHand(p_41434_);
        FoodProperties foodproperties = itemstack.get(DataComponents.FOOD);
        if (foodproperties != null) {
            if (p_41433_.canEat(foodproperties.canAlwaysEat())) {
                p_41433_.startUsingItem(p_41434_);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(p_41433_.getItemInHand(p_41434_));
        }
    }

    public ItemStack finishUsingItem(ItemStack p_41409_, Level p_41410_, LivingEntity p_41411_) {
        FoodProperties foodproperties = p_41409_.get(DataComponents.FOOD);
        return foodproperties != null ? p_41411_.eat(p_41410_, p_41409_, foodproperties) : p_41409_;
    }

    public boolean isBarVisible(ItemStack p_150899_) {
        return p_150899_.isDamaged();
    }

    public int getBarWidth(ItemStack p_150900_) {
        return Mth.clamp(Math.round(13.0F - (float)p_150900_.getDamageValue() * 13.0F / (float)p_150900_.getMaxDamage()), 0, 13);
    }

    public int getBarColor(ItemStack p_150901_) {
        int i = p_150901_.getMaxDamage();
        float f = Math.max(0.0F, ((float)i - (float)p_150901_.getDamageValue()) / (float)i);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack p_150888_, Slot p_150889_, ClickAction p_150890_, Player p_150891_) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack p_150892_, ItemStack p_150893_, Slot p_150894_, ClickAction p_150895_, Player p_150896_, SlotAccess p_150897_) {
        return false;
    }

    public float getAttackDamageBonus(Entity p_345227_, float p_327880_, DamageSource p_342960_) {
        return 0.0F;
    }

    public boolean hurtEnemy(ItemStack p_41395_, LivingEntity p_41396_, LivingEntity p_41397_) {
        return false;
    }

    public void postHurtEnemy(ItemStack p_343373_, LivingEntity p_342300_, LivingEntity p_344220_) {
    }

    public boolean mineBlock(ItemStack p_41416_, Level p_41417_, BlockState p_41418_, BlockPos p_41419_, LivingEntity p_41420_) {
        Tool tool = p_41416_.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        } else {
            if (!p_41417_.isClientSide && p_41418_.getDestroySpeed(p_41417_, p_41419_) != 0.0F && tool.damagePerBlock() > 0) {
                p_41416_.hurtAndBreak(tool.damagePerBlock(), p_41420_, EquipmentSlot.MAINHAND);
            }

            return true;
        }
    }

    public boolean isCorrectToolForDrops(ItemStack p_332232_, BlockState p_41450_) {
        Tool tool = p_332232_.get(DataComponents.TOOL);
        return tool != null && tool.isCorrectForDrops(p_41450_);
    }

    public InteractionResult interactLivingEntity(ItemStack p_41398_, Player p_41399_, LivingEntity p_41400_, InteractionHand p_41401_) {
        return InteractionResult.PASS;
    }

    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public String toString() {
        return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack p_41455_) {
        return this.getDescriptionId();
    }

    @Nullable
    @Deprecated // Use ItemStack sensitive version.
    public final Item getCraftingRemainingItem() {
        return this.craftingRemainingItem;
    }

    @Deprecated // Use ItemStack sensitive version.
    public boolean hasCraftingRemainingItem() {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
    }

    public void onCraftedBy(ItemStack p_41447_, Level p_41448_, Player p_41449_) {
        this.onCraftedPostProcess(p_41447_, p_41448_);
    }

    public void onCraftedPostProcess(ItemStack p_312780_, Level p_312645_) {
    }

    public boolean isComplex() {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return p_41452_.has(DataComponents.FOOD) ? UseAnim.EAT : UseAnim.NONE;
    }

    public int getUseDuration(ItemStack p_41454_, LivingEntity p_342054_) {
        FoodProperties foodproperties = p_41454_.get(DataComponents.FOOD);
        return foodproperties != null ? foodproperties.eatDurationTicks() : 0;
    }

    public void releaseUsing(ItemStack p_41412_, Level p_41413_, LivingEntity p_41414_, int p_41415_) {
    }

    public void appendHoverText(ItemStack p_41421_, Item.TooltipContext p_333372_, List<Component> p_41423_, TooltipFlag p_41424_) {
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack p_150902_) {
        return Optional.empty();
    }

    public Component getName(ItemStack p_41458_) {
        return Component.translatable(this.getDescriptionId(p_41458_));
    }

    public boolean isFoil(ItemStack p_41453_) {
        return p_41453_.isEnchanted();
    }

    public boolean isEnchantable(ItemStack p_41456_) {
        return p_41456_.getMaxStackSize() == 1 && p_41456_.has(DataComponents.MAX_DAMAGE);
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level p_41436_, Player p_41437_, ClipContext.Fluid p_41438_) {
        Vec3 vec3 = p_41437_.getEyePosition();
        Vec3 vec31 = vec3.add(p_41437_.calculateViewVector(p_41437_.getXRot(), p_41437_.getYRot()).scale(p_41437_.blockInteractionRange()));
        return p_41436_.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, p_41438_, p_41437_));
    }

    /** @deprecated in favor of {@link net.minecraftforge.common.extensions.IForgeItem#getEnchantmentValue(ItemStack)}  */
    public int getEnchantmentValue() {
        return 0;
    }

    public boolean isValidRepairItem(ItemStack p_41402_, ItemStack p_41403_) {
        return false;
    }

    /** @deprecated in favor of {@link net.minecraftforge.common.extensions.IForgeItem#getDefaultAttributeModifiers(ItemStack)}  */
    @Deprecated
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.EMPTY;
    }

    public boolean useOnRelease(ItemStack p_41464_) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getBreakingSound() {
        return SoundEvents.ITEM_BREAK;
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    private Object renderProperties;

    /*
       DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
       Call RenderProperties.get instead
     */
    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }

    private void initClient() {
        // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData()) {
            initializeClient(properties -> {
                if (properties == this) {
                    throw new IllegalStateException("Don't extend IItemRenderProperties in your item, use an anonymous class instead.");
                }
                this.renderProperties = properties;
            });
        }
    }

    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) { }

    public static class Properties {
        private static final Interner<DataComponentMap> COMPONENT_INTERNER = Interners.newStrongInterner();
        @Nullable
        private DataComponentMap.Builder components;
        @Nullable
        Item craftingRemainingItem;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

        public Item.Properties food(FoodProperties p_41490_) {
            return this.component(DataComponents.FOOD, p_41490_);
        }

        public Item.Properties stacksTo(int p_41488_) {
            return this.component(DataComponents.MAX_STACK_SIZE, p_41488_);
        }

        public Item.Properties durability(int p_41504_) {
            this.component(DataComponents.MAX_DAMAGE, p_41504_);
            this.component(DataComponents.MAX_STACK_SIZE, 1);
            this.component(DataComponents.DAMAGE, 0);
            return this;
        }

        public Item.Properties craftRemainder(Item p_41496_) {
            this.craftingRemainingItem = p_41496_;
            return this;
        }

        public Item.Properties rarity(Rarity p_41498_) {
            return this.component(DataComponents.RARITY, p_41498_);
        }

        public Item.Properties fireResistant() {
            return this.component(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }

        public Item.Properties jukeboxPlayable(ResourceKey<JukeboxSong> p_342377_) {
            return this.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(p_342377_), true));
        }

        public Item.Properties requiredFeatures(FeatureFlag... p_250948_) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(p_250948_);
            return this;
        }

        public <T> Item.Properties component(DataComponentType<T> p_333852_, T p_330859_) {
            if (this.components == null) {
                this.components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
            }

            this.components.set(p_333852_, p_330859_);
            return this;
        }

        public Item.Properties attributes(ItemAttributeModifiers p_330293_) {
            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, p_330293_);
        }

        DataComponentMap buildAndValidateComponents() {
            DataComponentMap datacomponentmap = this.buildComponents();
            if (datacomponentmap.has(DataComponents.DAMAGE) && datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            } else {
                return datacomponentmap;
            }
        }

        private DataComponentMap buildComponents() {
            return this.components == null ? DataComponents.COMMON_ITEM_COMPONENTS : COMPONENT_INTERNER.intern(this.components.build());
        }
    }

    public interface TooltipContext {
        Item.TooltipContext EMPTY = new Item.TooltipContext() {
            @Nullable
            @Override
            public HolderLookup.Provider registries() {
                return null;
            }

            @Override
            public float tickRate() {
                return 20.0F;
            }

            @Nullable
            @Override
            public MapItemSavedData mapData(MapId p_334227_) {
                return null;
            }
        };

        @Nullable
        HolderLookup.Provider registries();

        float tickRate();

        @Nullable
        MapItemSavedData mapData(MapId p_335695_);

        @Nullable
        default Level level() {
            return null;
        }

        static Item.TooltipContext of(@Nullable final Level p_332083_) {
            return p_332083_ == null ? EMPTY : new Item.TooltipContext() {
                @Override
                public HolderLookup.Provider registries() {
                    return p_332083_.registryAccess();
                }

                @Override
                public float tickRate() {
                    return p_332083_.tickRateManager().tickrate();
                }

                @Override
                public MapItemSavedData mapData(MapId p_330171_) {
                    return p_332083_.getMapData(p_330171_);
                }

                @Override
                public Level level() {
                    return p_332083_;
                }
            };
        }

        static Item.TooltipContext of(final HolderLookup.Provider p_335652_) {
            return new Item.TooltipContext() {
                @Override
                public HolderLookup.Provider registries() {
                    return p_335652_;
                }

                @Override
                public float tickRate() {
                    return 20.0F;
                }

                @Nullable
                @Override
                public MapItemSavedData mapData(MapId p_332386_) {
                    return null;
                }
            };
        }
    }
}
