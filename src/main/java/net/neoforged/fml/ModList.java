package net.neoforged.fml;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Proxy for NeoForge's {@code ModList}.
 * Delegates to Forge's {@link net.minecraftforge.fml.ModList}.
 */
public class ModList {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Registry of NeoForge mod file info for mods not in Forge's ModList. */
    private static final Map<String, net.neoforged.neoforgespi.language.IModFileInfo> neoModFileInfo
            = new ConcurrentHashMap<>();

    /**
     * Register a NeoForge mod's file info so getModFileById() can find it.
     */
    public static void registerNeoModFileInfo(String modId, net.neoforged.neoforgespi.language.IModFileInfo info) {
        neoModFileInfo.put(modId, info);
        LOGGER.info("[ReForged] Registered NeoModFileInfo for '{}', map now has {} entries: {}",
                modId, neoModFileInfo.size(), neoModFileInfo.keySet());
    }

    /**
     * Look up a NeoForge mod's file info (for cross-package access).
     */
    public static net.neoforged.neoforgespi.language.IModFileInfo neoModFileInfoLookup(String modId) {
        return neoModFileInfo.get(modId);
    }

    /**
     * Get the active mod list.
     */
    public static ModList get() {
        return new ModList();
    }

    /**
     * Check if a mod is loaded.
     */
    public boolean isLoaded(String modid) {
        return net.minecraftforge.fml.ModList.get().isLoaded(modid);
    }

    /**
     * Get a mod container by ID.
     * Returns a NeoForge ModContainer wrapper so NeoForge mods can use it directly.
     */
    public Optional<ModContainer> getModContainerById(String modid) {
        return net.minecraftforge.fml.ModList.get().getModContainerById(modid)
                .map(ModContainer::wrap);
    }

    /**
     * Get all mod IDs.
     */
    public List<String> getMods() {
        return net.minecraftforge.fml.ModList.get().getMods().stream()
                .map(info -> info.getModId())
                .toList();
    }

    /**
     * Run for each mod container.
     */
    public void forEachModContainer(java.util.function.BiConsumer<String, net.minecraftforge.fml.ModContainer> consumer) {
        net.minecraftforge.fml.ModList.get().forEachModContainer(consumer);
    }

    /**
     * Get all scan data for loaded mods.
     *
     * NeoForge mods (e.g. Jade) use this during load-complete scanning.
     */
    public List<net.neoforged.neoforgespi.language.ModFileScanData> getAllScanData() {
        return net.minecraftforge.fml.ModList.get().getAllScanData().stream()
                .map(net.neoforged.neoforgespi.language.ModFileScanData::wrap)
                .collect(Collectors.toList());
    }

    /**
     * Get mod file info by mod ID.
     * Delegates to Forge's ModList and wraps the result.
     * Falls back to NeoForge mod registry if not found in Forge.
     */
    public net.neoforged.neoforgespi.language.IModFileInfo getModFileById(String modid) {
        net.minecraftforge.forgespi.language.IModFileInfo forgeInfo =
                net.minecraftforge.fml.ModList.get().getModFileById(modid);
        if (forgeInfo != null) {
            return net.neoforged.neoforgespi.language.IModFileInfo.wrap(forgeInfo);
        }
        // Fallback: check NeoForge mods registered via registerNeoModFileInfo()
        var result = neoModFileInfo.get(modid);
        if (result == null) {
            // For 'neoforge' / 'forge' lookups, return a stub wrapping Forge's own mod file info
            if ("neoforge".equals(modid) || "forge".equals(modid)) {
                net.minecraftforge.forgespi.language.IModFileInfo forgeSelf =
                        net.minecraftforge.fml.ModList.get().getModFileById("forge");
                if (forgeSelf != null) {
                    return net.neoforged.neoforgespi.language.IModFileInfo.wrap(forgeSelf);
                }
            }
            LOGGER.warn("[ReForged] getModFileById('{}') returned null. neoModFileInfo keys: {}",
                    modid, neoModFileInfo.keySet());
        }
        return result;
    }
}
