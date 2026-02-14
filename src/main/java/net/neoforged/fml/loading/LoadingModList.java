package net.neoforged.fml.loading;

import java.util.Collections;
import java.util.List;

/**
 * Stub for NeoForge's LoadingModList.
 * Provides access to mod files during the loading process.
 */
public class LoadingModList {
    private static final LoadingModList INSTANCE = new LoadingModList();

    public static LoadingModList get() {
        return INSTANCE;
    }

    /**
     * Get mod files — stub returns empty list.
     */
    public List<?> getModFiles() {
        return Collections.emptyList();
    }

    /**
     * Get errors during loading — stub returns empty list.
     */
    public List<?> getErrors() {
        return Collections.emptyList();
    }

    /**
     * Get mod file info by mod ID.
     * Delegates to Forge's LoadingModList.
     */
    public net.neoforged.fml.loading.moddiscovery.ModFileInfo getModFileById(String modid) {
        // Try to get from Forge's LoadingModList
        try {
            var forgeList = net.minecraftforge.fml.loading.LoadingModList.get();
            if (forgeList != null) {
                var forgeInfo = forgeList.getModFileById(modid);
                if (forgeInfo != null) {
                    return new net.neoforged.fml.loading.moddiscovery.ModFileInfo(forgeInfo);
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // Fallback: check NeoForge ModList registry
        net.neoforged.neoforgespi.language.IModFileInfo neoInfo =
                net.neoforged.fml.ModList.neoModFileInfoLookup(modid);
        if (neoInfo != null) {
            // Return a stub ModFileInfo wrapping null — callers will use it via IModFileInfo interface
            return new net.neoforged.fml.loading.moddiscovery.ModFileInfo(null);
        }
        return null;
    }
}
