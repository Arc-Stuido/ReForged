package net.neoforged.fml.loading.moddiscovery;

import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

/**
 * Stub for NeoForge's {@code net.neoforged.fml.loading.moddiscovery.ModInfo}.
 * <p>
 * Twilight Forest's {@code ModUpdateURLInterceptor} does an {@code instanceof ModInfo}
 * check and then reflectively writes to the {@code updateJSONURL} field.
 * This class provides the concrete type and field needed for that to work.
 */
public class ModInfo implements IModInfo {

    private final net.minecraftforge.forgespi.language.IModInfo delegate;

    /** Reflectively accessed by TF's ModUpdateURLInterceptor via MethodHandle setter. */
    @SuppressWarnings("unused")
    private Optional<URL> updateJSONURL;

    public ModInfo(net.minecraftforge.forgespi.language.IModInfo forgeInfo) {
        this.delegate = forgeInfo;
        this.updateJSONURL = forgeInfo.getUpdateURL();
    }

    @Override
    public String getModId() {
        return delegate.getModId();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public ArtifactVersion getVersion() {
        return delegate.getVersion();
    }

    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public Map<String, Object> getModProperties() {
        return delegate.getModProperties();
    }

    @Override
    public Optional<URL> getUpdateURL() {
        return updateJSONURL != null ? updateJSONURL : delegate.getUpdateURL();
    }

    @Override
    public Optional<String> getLogoFile() {
        return delegate.getLogoFile();
    }

    @Override
    public boolean getLogoBlur() {
        return delegate.getLogoBlur();
    }

    @Override
    public IModFileInfo getOwningFile() {
        return IModFileInfo.wrap(delegate.getOwningFile());
    }
}
