package net.neoforged.neoforge.fluids;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundAction;

/**
 * NeoForge FluidType shim backed by Forge's FluidType implementation.
 */
public class FluidType extends net.minecraftforge.fluids.FluidType {
    private static final Map<net.minecraftforge.fluids.FluidType, FluidType> WRAPPER_CACHE =
            new ConcurrentHashMap<>();

    @Nullable
    public static FluidType wrap(@Nullable net.minecraftforge.fluids.FluidType forgeType) {
        if (forgeType == null) return null;
        if (forgeType instanceof FluidType neo) return neo;
        return WRAPPER_CACHE.computeIfAbsent(forgeType, ForgeFluidTypeWrapper::new);
    }

    public FluidType(net.minecraftforge.fluids.FluidType.Properties properties) {
        super(properties);
    }

    public FluidType(Properties properties) {
        super(properties.delegate);
    }

    private static final class ForgeFluidTypeWrapper extends FluidType {
        private final net.minecraftforge.fluids.FluidType delegate;

        private ForgeFluidTypeWrapper(net.minecraftforge.fluids.FluidType delegate) {
            super(copyProperties(delegate));
            this.delegate = delegate;
        }

        @Override
        public Component getDescription() {
            return delegate.getDescription();
        }

        @Override
        public String getDescriptionId() {
            return delegate.getDescriptionId();
        }

        @Override
        public int getLightLevel() {
            return delegate.getLightLevel();
        }

        @Override
        public int getDensity() {
            return delegate.getDensity();
        }

        @Override
        public int getTemperature() {
            return delegate.getTemperature();
        }

        @Override
        public int getViscosity() {
            return delegate.getViscosity();
        }

        @Override
        public Rarity getRarity() {
            return delegate.getRarity();
        }

        @Nullable
        @Override
        public SoundEvent getSound(SoundAction action) {
            return delegate.getSound(action);
        }

        public SoundEvent getSound(net.neoforged.neoforge.common.SoundAction action) {
            return delegate.getSound(SoundAction.get(action.name()));
        }

        @Override
        public double motionScale(Entity entity) {
            return delegate.motionScale(entity);
        }

        @Override
        public boolean canPushEntity(Entity entity) {
            return delegate.canPushEntity(entity);
        }

        @Override
        public boolean canSwim(Entity entity) {
            return delegate.canSwim(entity);
        }

        @Override
        public float getFallDistanceModifier(Entity entity) {
            return delegate.getFallDistanceModifier(entity);
        }

        @Override
        public boolean canExtinguish(Entity entity) {
            return delegate.canExtinguish(entity);
        }

        @Override
        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            return delegate.move(state, entity, movementVector, gravity);
        }

        @Override
        public boolean canDrownIn(LivingEntity entity) {
            return delegate.canDrownIn(entity);
        }

        @Override
        public void setItemMovement(ItemEntity entity) {
            delegate.setItemMovement(entity);
        }

        @Override
        public boolean supportsBoating(Boat boat) {
            return delegate.supportsBoating(boat);
        }

        @Override
        public boolean supportsBoating(FluidState state, Boat boat) {
            return delegate.supportsBoating(state, boat);
        }

        @Override
        public boolean shouldUpdateWhileBoating(FluidState state, Boat boat, Entity rider) {
            return delegate.shouldUpdateWhileBoating(state, boat, rider);
        }

        @Override
        public boolean canRideVehicleUnder(Entity vehicle, Entity rider) {
            return delegate.canRideVehicleUnder(vehicle, rider);
        }

        @Override
        public boolean canHydrate(Entity entity) {
            return delegate.canHydrate(entity);
        }

        @Nullable
        @Override
        public SoundEvent getSound(Entity entity, SoundAction action) {
            return delegate.getSound(entity, action);
        }

        @Override
        public boolean canExtinguish(FluidState state, BlockGetter getter, BlockPos pos) {
            return delegate.canExtinguish(state, getter, pos);
        }

        @Override
        public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
            return delegate.canConvertToSource(state, reader, pos);
        }

        @Nullable
        @Override
        public PathType getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
            return delegate.getBlockPathType(state, level, pos, mob, canFluidLog);
        }

        @Nullable
        @Override
        public PathType getAdjacentBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
            return delegate.getAdjacentBlockPathType(state, level, pos, mob, originalType);
        }

        @Nullable
        @Override
        public SoundEvent getSound(@Nullable Player player, BlockGetter getter, BlockPos pos, SoundAction action) {
            return delegate.getSound(player, getter, pos, action);
        }

        @Override
        public boolean canHydrate(FluidState state, BlockGetter getter, BlockPos pos, BlockState source, BlockPos sourcePos) {
            return delegate.canHydrate(state, getter, pos, source, sourcePos);
        }

        @Override
        public int getLightLevel(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return delegate.getLightLevel(state, getter, pos);
        }

        @Override
        public int getDensity(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return delegate.getDensity(state, getter, pos);
        }

        @Override
        public int getTemperature(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return delegate.getTemperature(state, getter, pos);
        }

        @Override
        public int getViscosity(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return delegate.getViscosity(state, getter, pos);
        }

        @Override
        public boolean canConvertToSource(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.canConvertToSource(stack);
        }

        public boolean canConvertToSource(FluidStack stack) {
            return delegate.canConvertToSource(toForgeStack(stack));
        }

        @Nullable
        @Override
        public SoundEvent getSound(net.minecraftforge.fluids.FluidStack stack, SoundAction action) {
            return delegate.getSound(stack, action);
        }

        @Nullable
        public SoundEvent getSound(FluidStack stack, SoundAction action) {
            return delegate.getSound(toForgeStack(stack), action);
        }

        public SoundEvent getSound(FluidStack stack, net.neoforged.neoforge.common.SoundAction action) {
            return delegate.getSound(toForgeStack(stack), SoundAction.get(action.name()));
        }

        @Override
        public Component getDescription(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getDescription(stack);
        }

        public Component getDescription(FluidStack stack) {
            return delegate.getDescription(toForgeStack(stack));
        }

        @Override
        public String getDescriptionId(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getDescriptionId(stack);
        }

        public String getDescriptionId(FluidStack stack) {
            return delegate.getDescriptionId(toForgeStack(stack));
        }

        @Override
        public boolean canHydrate(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.canHydrate(stack);
        }

        public boolean canHydrate(FluidStack stack) {
            return delegate.canHydrate(toForgeStack(stack));
        }

        @Override
        public int getLightLevel(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getLightLevel(stack);
        }

        public int getLightLevel(FluidStack stack) {
            return delegate.getLightLevel(toForgeStack(stack));
        }

        @Override
        public int getDensity(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getDensity(stack);
        }

        public int getDensity(FluidStack stack) {
            return delegate.getDensity(toForgeStack(stack));
        }

        @Override
        public int getTemperature(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getTemperature(stack);
        }

        public int getTemperature(FluidStack stack) {
            return delegate.getTemperature(toForgeStack(stack));
        }

        @Override
        public int getViscosity(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getViscosity(stack);
        }

        public int getViscosity(FluidStack stack) {
            return delegate.getViscosity(toForgeStack(stack));
        }

        @Override
        public Rarity getRarity(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getRarity(stack);
        }

        public Rarity getRarity(FluidStack stack) {
            return delegate.getRarity(toForgeStack(stack));
        }

        @Override
        public ItemStack getBucket(net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getBucket(stack);
        }

        public ItemStack getBucket(FluidStack stack) {
            return delegate.getBucket(toForgeStack(stack));
        }

        @Override
        public BlockState getBlockForFluidState(BlockAndTintGetter getter, BlockPos pos, FluidState state) {
            return delegate.getBlockForFluidState(getter, pos, state);
        }

        @Override
        public FluidState getStateForPlacement(BlockAndTintGetter getter, BlockPos pos, net.minecraftforge.fluids.FluidStack stack) {
            return delegate.getStateForPlacement(getter, pos, stack);
        }

        public FluidState getStateForPlacement(BlockAndTintGetter getter, BlockPos pos, FluidStack stack) {
            return delegate.getStateForPlacement(getter, pos, toForgeStack(stack));
        }

        @Override
        public boolean isVaporizedOnPlacement(Level level, BlockPos pos, net.minecraftforge.fluids.FluidStack stack) {
            return delegate.isVaporizedOnPlacement(level, pos, stack);
        }

        public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
            return delegate.isVaporizedOnPlacement(level, pos, toForgeStack(stack));
        }

        @Override
        public void onVaporize(@Nullable Player player, Level level, BlockPos pos, net.minecraftforge.fluids.FluidStack stack) {
            delegate.onVaporize(player, level, pos, stack);
        }

        public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
            delegate.onVaporize(player, level, pos, toForgeStack(stack));
        }

        @Override
        public Object getRenderPropertiesInternal() {
            return delegate.getRenderPropertiesInternal();
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            if (delegate == null) {
                return;
            }
            delegate.initializeClient(consumer);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ForgeFluidTypeWrapper wrapper
                    ? delegate.equals(wrapper.delegate)
                    : delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    private static net.minecraftforge.fluids.FluidStack toForgeStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount());
    }

    private static net.minecraftforge.fluids.FluidType.Properties copyProperties(net.minecraftforge.fluids.FluidType delegate) {
        net.minecraftforge.fluids.FluidType.Properties properties = net.minecraftforge.fluids.FluidType.Properties.create();

        String descriptionId = read(delegate, "descriptionId", null);
        if (descriptionId != null) properties.descriptionId(descriptionId);
        properties.motionScale(read(delegate, "motionScale", 0.014D));
        properties.canPushEntity(read(delegate, "canPushEntity", true));
        properties.canSwim(read(delegate, "canSwim", true));
        properties.canDrown(read(delegate, "canDrown", true));
        properties.fallDistanceModifier(read(delegate, "fallDistanceModifier", 0.5F));
        properties.canExtinguish(read(delegate, "canExtinguish", false));
        properties.canConvertToSource(read(delegate, "canConvertToSource", false));
        properties.supportsBoating(read(delegate, "supportsBoating", true));
        PathType pathType = read(delegate, "pathType", null);
        if (pathType != null) properties.pathType(pathType);
        PathType adjacentPathType = read(delegate, "adjacentPathType", null);
        if (adjacentPathType != null) properties.adjacentPathType(adjacentPathType);
        properties.canHydrate(read(delegate, "canHydrate", false));
        properties.lightLevel(read(delegate, "lightLevel", 0));
        properties.density(read(delegate, "density", 1000));
        properties.temperature(read(delegate, "temperature", 300));
        properties.viscosity(read(delegate, "viscosity", 1000));
        properties.rarity(read(delegate, "rarity", Rarity.COMMON));

        Map<SoundAction, SoundEvent> sounds = read(delegate, "sounds", Map.of());
        sounds.forEach(properties::sound);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static <T> T read(net.minecraftforge.fluids.FluidType type, String fieldName, T fallback) {
        try {
            Field field = net.minecraftforge.fluids.FluidType.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(type);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static final class Properties {
        final net.minecraftforge.fluids.FluidType.Properties delegate;

        private Properties(net.minecraftforge.fluids.FluidType.Properties delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        public static Properties create() {
            return new Properties(net.minecraftforge.fluids.FluidType.Properties.create());
        }

        public Properties descriptionId(String descriptionId) { delegate.descriptionId(descriptionId); return this; }
        public Properties motionScale(double motionScale) { delegate.motionScale(motionScale); return this; }
        public Properties canPushEntity(boolean canPushEntity) { delegate.canPushEntity(canPushEntity); return this; }
        public Properties canSwim(boolean canSwim) { delegate.canSwim(canSwim); return this; }
        public Properties canDrown(boolean canDrown) { delegate.canDrown(canDrown); return this; }
        public Properties fallDistanceModifier(float fallDistanceModifier) { delegate.fallDistanceModifier(fallDistanceModifier); return this; }
        public Properties canExtinguish(boolean canExtinguish) { delegate.canExtinguish(canExtinguish); return this; }
        public Properties canConvertToSource(boolean canConvertToSource) { delegate.canConvertToSource(canConvertToSource); return this; }
        public Properties supportsBoating(boolean supportsBoating) { delegate.supportsBoating(supportsBoating); return this; }
        public Properties pathType(PathType pathType) { delegate.pathType(pathType); return this; }
        public Properties adjacentPathType(PathType adjacentPathType) { delegate.adjacentPathType(adjacentPathType); return this; }
        public Properties sound(SoundAction action, SoundEvent sound) { delegate.sound(action, sound); return this; }
        public Properties sound(net.neoforged.neoforge.common.SoundAction action, SoundEvent sound) {
            delegate.sound(SoundAction.get(action.name()), sound);
            return this;
        }
        public Properties canHydrate(boolean canHydrate) { delegate.canHydrate(canHydrate); return this; }
        public Properties lightLevel(int lightLevel) { delegate.lightLevel(lightLevel); return this; }
        public Properties density(int density) { delegate.density(density); return this; }
        public Properties temperature(int temperature) { delegate.temperature(temperature); return this; }
        public Properties viscosity(int viscosity) { delegate.viscosity(viscosity); return this; }
        public Properties rarity(Rarity rarity) { delegate.rarity(rarity); return this; }
    }
}
