package net.neoforged.neoforgespi.language;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shim: NeoForge's IModInfo interface.
 * <p>
 * Provides mod metadata such as mod ID, display name, version, etc.
 * Our implementation wraps Forge's IModInfo.
 */
public interface IModInfo {

    String getModId();

    String getDisplayName();

    String getDescription();

    ArtifactVersion getVersion();

    String getNamespace();

    Map<String, Object> getModProperties();

    Optional<URL> getUpdateURL();

    Optional<String> getLogoFile();

    boolean getLogoBlur();

    IModFileInfo getOwningFile();

    /**
     * Wraps a Forge IModInfo into a NeoForge IModInfo.
     * Returns a {@link net.neoforged.fml.loading.moddiscovery.ModInfo} concrete class
     * so that {@code instanceof ModInfo} checks used by TF work correctly.
     */
    static IModInfo wrap(net.minecraftforge.forgespi.language.IModInfo forgeInfo) {
        return new net.neoforged.fml.loading.moddiscovery.ModInfo(forgeInfo);
    }
}
