package net.neoforged.neoforge.fluids;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Shim: NeoForge's FluidType → delegates to Forge's FluidType.
 */
public class FluidType extends net.minecraftforge.fluids.FluidType {

    public FluidType(net.minecraftforge.fluids.FluidType.Properties properties) {
        super(properties);
    }

    public FluidType(Properties properties) {
        super(properties.delegate);
    }

    /**
     * Wrapper around Forge's FluidType.Properties since that class is final
     * and inner classes are not inherited.
     */
    public static final class Properties {
        final net.minecraftforge.fluids.FluidType.Properties delegate;

        private Properties(net.minecraftforge.fluids.FluidType.Properties delegate) {
            this.delegate = delegate;
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
        public Properties sound(net.minecraftforge.common.SoundAction action, SoundEvent sound) { delegate.sound(action, sound); return this; }
        public Properties canHydrate(boolean canHydrate) { delegate.canHydrate(canHydrate); return this; }
        public Properties lightLevel(int lightLevel) { delegate.lightLevel(lightLevel); return this; }
        public Properties density(int density) { delegate.density(density); return this; }
        public Properties temperature(int temperature) { delegate.temperature(temperature); return this; }
        public Properties viscosity(int viscosity) { delegate.viscosity(viscosity); return this; }
        public Properties rarity(Rarity rarity) { delegate.rarity(rarity); return this; }
    }
}
