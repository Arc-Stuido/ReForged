package net.neoforged.fml.loading.modscan;

/**
 * Stub: NeoForge ModAnnotation.
 * <p>
 * Provides the {@link EnumHolder} record used to represent enum values
 * found in annotation data during class scanning. Mods like Twilight Forest
 * expect enum annotation values to be stored as EnumHolder instances.
 */
public class ModAnnotation {

    /**
     * Holds a reference to an enum value found in annotation data.
     *
     * @param desc  The ASM type descriptor of the enum class (e.g. {@code "Lcom/example/MyEnum;"})
     * @param value The enum constant name (e.g. {@code "SOME_VALUE"})
     */
    public record EnumHolder(String desc, String value) {}
}
