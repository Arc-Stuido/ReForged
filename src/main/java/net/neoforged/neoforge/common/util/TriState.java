package net.neoforged.neoforge.common.util;

/**
 * Stub: NeoForge's TriState enum — TRUE / DEFAULT / FALSE.
 */
public enum TriState {
    TRUE,
    DEFAULT,
    FALSE;

    public boolean isTrue()    { return this == TRUE; }
    public boolean isDefault() { return this == DEFAULT; }
    public boolean isFalse()   { return this == FALSE; }
}
