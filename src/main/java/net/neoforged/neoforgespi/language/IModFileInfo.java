package net.neoforged.neoforgespi.language;

import net.neoforged.neoforgespi.locating.IModFile;

import java.util.List;
import java.util.Map;

/**
 * Shim: NeoForge's IModFileInfo interface.
 * <p>
 * Wraps Forge's {@code net.minecraftforge.forgespi.language.IModFileInfo}.
 */
public interface IModFileInfo {

    List<IModInfo> getMods();

    Map<String, Object> getFileProperties();

    String getLicense();

    String moduleName();

    String versionString();

    List<String> usesServices();

    IModFile getFile();

    /**
     * Wraps a Forge IModFileInfo into a NeoForge IModFileInfo.
     */
    static IModFileInfo wrap(net.minecraftforge.forgespi.language.IModFileInfo forgeFileInfo) {
        if (forgeFileInfo == null) return null;
        return new IModFileInfo() {
            @Override
            public List<IModInfo> getMods() {
                // Wrap each Forge IModInfo into NeoForge IModInfo
                return forgeFileInfo.getMods().stream()
                        .map(IModInfo::wrap)
                        .collect(java.util.stream.Collectors.toList());
            }

            @Override
            public Map<String, Object> getFileProperties() {
                return forgeFileInfo.getFileProperties();
            }

            @Override
            public String getLicense() {
                return forgeFileInfo.getLicense();
            }

            @Override
            public String moduleName() {
                return forgeFileInfo.moduleName();
            }

            @Override
            public String versionString() {
                return forgeFileInfo.versionString();
            }

            @Override
            public List<String> usesServices() {
                return forgeFileInfo.usesServices();
            }

            @Override
            public IModFile getFile() {
                return IModFile.wrap(forgeFileInfo.getFile());
            }
        };
    }
}
