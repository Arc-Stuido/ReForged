package net.minecraftforge.fml.common.asm.enumextension;

/**
 * Forge-namespace alias for rewritten NeoForge enum-extension proxies.
 */
public class EnumProxy<T extends Enum<T>> extends net.neoforged.fml.common.asm.enumextension.EnumProxy<T> {
    public EnumProxy(Class<T> enumClass, Object... constructorArgs) {
        super(enumClass, constructorArgs);
    }
}
