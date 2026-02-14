package net.neoforged.fml.loading;

import net.neoforged.api.distmarker.Dist;

/**
 * Proxy: NeoForge's FMLLoader.
 * <p>
 * Delegates to Forge's environment to provide the distribution and loading state
 * that NeoForge mods expect from this class.
 */
public class FMLLoader {

    /**
     * Returns the current distribution (CLIENT or DEDICATED_SERVER).
     */
    public static Dist getDist() {
        return FMLEnvironment.dist;
    }

    /**
     * Whether we are running in a production (non-dev) environment.
     */
    public static boolean isProduction() {
        return FMLEnvironment.production;
    }

    /**
     * Returns the game {@link ModuleLayer} that contains all loaded mods.
     * <p>
     * In Forge, we return the boot layer – it is the layer the mod classes are
     * visible from when running inside ForgeGradle / dev environment.
     */
    public static ModuleLayer getGameLayer() {
        return ModuleLayer.boot();
    }

    /**
     * Returns the Minecraft version info.
     */
    public static Object versionInfo() {
        return net.minecraftforge.fml.loading.FMLLoader.versionInfo();
    }
}
