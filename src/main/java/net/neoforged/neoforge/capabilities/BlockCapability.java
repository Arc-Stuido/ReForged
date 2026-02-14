package net.neoforged.neoforge.capabilities;

import java.util.ArrayList;
import java.util.Collections;
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
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's BlockCapability — capability lookup for blocks.
 */
public final class BlockCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
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

    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state,
                           @Nullable BlockEntity blockEntity, C context) {
        for (var provider : providers.getOrDefault(
                (state != null ? state : level.getBlockState(pos)).getBlock(), List.of())) {
            var ret = provider.getCapability(level, pos,
                    state != null ? state : level.getBlockState(pos), blockEntity, context);
            if (ret != null) return ret;
        }
        return null;
    }
}
