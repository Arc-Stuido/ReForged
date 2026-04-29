package net.neoforged.neoforge.registries;

import net.minecraft.core.IdMapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Map;
import org.xiyu.reforged.bridge.BlockStateIdMapBridge;

/**
 * Central registry data management.
 */
public class GameData {

    @SuppressWarnings("unchecked")
    public static Map<Block, Item> getBlockItemMap() {
        return (Map<Block, Item>) (Map<?, ?>) Item.BY_BLOCK;
    }

    public static IdMapper<BlockState> getBlockStateIDMap() {
        return BlockStateIdMapBridge.fallbackBlockStateIdMap();
    }

    private GameData() {}
}
