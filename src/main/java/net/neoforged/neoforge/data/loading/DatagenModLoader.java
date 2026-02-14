package net.neoforged.neoforge.data.loading;

/**
 * Shim: NeoForge's DatagenModLoader.
 * <p>
 * Registrate and other mods check {@code DatagenModLoader.isRunningDataGen()}
 * to decide whether to register data providers. At runtime this is always false.
 */
public class DatagenModLoader {

    private DatagenModLoader() {}

    /**
     * @return {@code true} if data generation is currently running; always {@code false} at runtime.
     */
    public static boolean isRunningDataGen() {
        return false;
    }
}
