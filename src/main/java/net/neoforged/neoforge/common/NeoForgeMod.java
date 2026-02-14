package net.neoforged.neoforge.common;

public class NeoForgeMod {
    private static boolean milkFluidEnabled = false;

    /**
     * Stub: NeoForge method called by mods (e.g. Create) to enable milk as a fluid.
     * On Forge this is a no-op since Forge handles milk its own way.
     */
    public static void enableMilkFluid() {
        milkFluidEnabled = true;
    }
}
