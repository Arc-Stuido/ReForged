package net.neoforged.fml.loading.moddiscovery;

import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Stub for NeoForge's ModFileInfo.
 * Wraps Forge's ModFileInfo to provide the NeoForge-expected type.
 */
public class ModFileInfo implements IModFileInfo {

    private final net.minecraftforge.fml.loading.moddiscovery.ModFileInfo forgeDelegate;

    public ModFileInfo(net.minecraftforge.fml.loading.moddiscovery.ModFileInfo forgeDelegate) {
        this.forgeDelegate = forgeDelegate;
    }

    @Override
    public List<IModInfo> getMods() {
        if (forgeDelegate == null) return Collections.emptyList();
        return forgeDelegate.getMods().stream()
                .map(IModInfo::wrap)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> getFileProperties() {
        if (forgeDelegate == null) return Collections.emptyMap();
        return forgeDelegate.getFileProperties();
    }

    @Override
    public String getLicense() {
        if (forgeDelegate == null) return "";
        return forgeDelegate.getLicense();
    }

    @Override
    public String moduleName() {
        if (forgeDelegate == null) return "";
        return forgeDelegate.moduleName();
    }

    @Override
    public String versionString() {
        if (forgeDelegate == null) return "0.0.0";
        return forgeDelegate.versionString();
    }

    @Override
    public List<String> usesServices() {
        if (forgeDelegate == null) return Collections.emptyList();
        return forgeDelegate.usesServices();
    }

    @Override
    public IModFile getFile() {
        if (forgeDelegate == null) return null;
        return IModFile.wrap(forgeDelegate.getFile());
    }
}
