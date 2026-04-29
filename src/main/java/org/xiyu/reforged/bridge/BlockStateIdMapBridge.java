package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import org.slf4j.Logger;

/**
 * Stable fallback for Forge's block-state ID map during early NeoForge registry
 * callbacks. Some NeoForge mods initialize entity classes while Forge's block
 * registry slave map is not yet reachable; Block's static final mapper must
 * still receive a non-null object that can be populated later.
 */
public final class BlockStateIdMapBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ClearableIdMapper<BlockState> FALLBACK = new ClearableIdMapper<>();
    private static boolean fallbackExposed;

    private BlockStateIdMapBridge() {}

    public static IdMapper<BlockState> fallbackBlockStateIdMap() {
        if (!fallbackExposed) {
            LOGGER.info("[ReForged] Using fallback block-state ID map for early NeoForge registry callbacks");
        }
        fallbackExposed = true;
        return FALLBACK;
    }

    public static void clearFallback() {
        FALLBACK.clear();
    }

    public static void rebuildFallback(Iterable<Block> blocks) {
        if (!fallbackExposed) {
            return;
        }
        FALLBACK.clear();
        int states = 0;
        for (Block block : blocks) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                FALLBACK.add(state);
                state.initCache();
                states++;
            }
            block.getDescriptionId();
        }
        DebugLevelSource.initValidStates();
        LOGGER.info("[ReForged] Rebuilt fallback block-state ID map with {} state(s)", states);
    }

    private static final class ClearableIdMapper<T> extends IdMapper<T> {
        private void clear() {
            this.tToId.clear();
            this.idToT.clear();
            this.nextId = 0;
        }
    }
}
