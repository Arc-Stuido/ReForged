package net.neoforged.neoforge.capabilities;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.xiyu.reforged.shim.capabilities.ForgeCapabilityBridge;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * NeoForge's BlockCapability — capability lookup for blocks.
 * <p>Bridges to Forge's capability system via {@link ForgeCapabilityBridge} when no
 * NeoForge provider is registered.</p>
 */
public final class BlockCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, BlockCapability<?, ?>> registry = new ConcurrentHashMap<>();

    final Map<Block, List<IBlockCapabilityProvider<T, C>>> providers = new IdentityHashMap<>();

    private BlockCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    @SuppressWarnings("unchecked")
    public static <T, C extends @Nullable Object> BlockCapability<T, C> create(
            ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (BlockCapability<T, C>) registry.computeIfAbsent(name,
                n -> new BlockCapability<>(n, typeClass, contextClass));
    }

    @SuppressWarnings("unchecked")
    public static <T> BlockCapability<T, @Nullable Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return (BlockCapability<T, @Nullable Void>) create(name, typeClass, (Class<@Nullable Void>) Void.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> BlockCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return (BlockCapability<T, @Nullable Direction>) create(name, typeClass, (Class<@Nullable Direction>) Direction.class);
    }

    public static synchronized List<BlockCapability<?, ?>> getAll() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Query a block capability in the world.
     * <ol>
     *   <li>First checks registered NeoForge-style providers</li>
     *   <li>Falls back to Forge's ICapabilityProvider on the BlockEntity</li>
     * </ol>
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state,
                           @Nullable BlockEntity blockEntity, C context) {
        BlockState resolvedState = state != null ? state : level.getBlockState(pos);
        Block block = resolvedState.getBlock();

        // 1. Try NeoForge-style providers
        for (var provider : providers.getOrDefault(block, List.of())) {
            var ret = provider.getCapability(level, pos, resolvedState, blockEntity, context);
            if (ret != null) return ret;
        }

        // 2. Resolve the block entity if needed
        BlockEntity be = blockEntity;
        if (be == null) {
            be = level.getBlockEntity(pos);
        }
        if (be == null) return null;

        // 3. Fall back to Forge capability system
        Direction side = (context instanceof Direction d) ? d : null;
        try {
            Capability<T> forgeCap = ForgeCapabilityBridge.findForgeCapability(name(), typeClass());
            if (forgeCap != null) {
                LazyOptional<T> lazyOpt = be.getCapability(forgeCap, side);
                if (lazyOpt.isPresent()) {
                    return lazyOpt.orElse(null);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] BlockCapability Forge fallback failed for {}: {}", name(), e.getMessage());
        }
        return null;
    }
}
