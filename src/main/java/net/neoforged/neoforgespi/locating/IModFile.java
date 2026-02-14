package net.neoforged.neoforgespi.locating;

import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.nio.file.Path;
import java.util.List;

/**
 * Shim: NeoForge's IModFile interface.
 * <p>
 * Wraps Forge's {@code net.minecraftforge.forgespi.locating.IModFile}.
 */
public interface IModFile {

    Path findResource(String... pathName);

    Path getFilePath();

    String getFileName();

    IModFileInfo getModFileInfo();

    List<IModInfo> getModInfos();

    ModFileScanData getScanResult();

    /**
     * Wraps a Forge IModFile into a NeoForge IModFile.
     */
    /**
     * Creates a stub IModFile with empty data, used when no real Forge IModFile exists
     * (e.g. for NeoForge mods loaded by ReForged that have no Forge-side file).
     */
    static IModFile empty() {
        return new IModFile() {
            @Override public Path findResource(String... pathName) { return null; }
            @Override public Path getFilePath() { return null; }
            @Override public String getFileName() { return "unknown"; }
            @Override public IModFileInfo getModFileInfo() { return null; }
            @Override public List<IModInfo> getModInfos() { return List.of(); }
            @Override public ModFileScanData getScanResult() { return new ModFileScanData(); }
        };
    }

    static IModFile wrap(net.minecraftforge.forgespi.locating.IModFile forgeFile) {
        if (forgeFile == null) return empty();
        return new IModFile() {
            @Override
            public Path findResource(String... pathName) {
                return forgeFile.findResource(pathName);
            }

            @Override
            public Path getFilePath() {
                return forgeFile.getFilePath();
            }

            @Override
            public String getFileName() {
                return forgeFile.getFileName();
            }

            @Override
            public IModFileInfo getModFileInfo() {
                return IModFileInfo.wrap(forgeFile.getModFileInfo());
            }

            @Override
            public List<IModInfo> getModInfos() {
                return forgeFile.getModInfos().stream()
                        .map(IModInfo::wrap)
                        .collect(java.util.stream.Collectors.toList());
            }

            @Override
            public ModFileScanData getScanResult() {
                return ModFileScanData.wrap(forgeFile.getScanResult());
            }
        };
    }
}
