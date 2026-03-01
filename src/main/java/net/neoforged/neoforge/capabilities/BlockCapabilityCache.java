package net.neoforged.neoforge.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A cache for block capabilities, to be used to track capabilities at a specific position.
 *
 * <p>In full NeoForge this integrates with Level invalidation listeners.
 * In ReForged we provide a simplified implementation that re-queries each call
 * but still exposes the same API surface that NeoForge mods expect.</p>
 */
public final class BlockCapabilityCache<T, C extends @Nullable Object> {

    /**
     * Creates a new cache instance (no invalidation listener).
     */
    public static <T, C extends @Nullable Object> BlockCapabilityCache<T, C> create(
            BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        return create(capability, level, pos, context, () -> true, () -> {});
    }

    /**
     * Creates a new cache instance with an invalidation listener.
     */
    public static <T, C extends @Nullable Object> BlockCapabilityCache<T, C> create(
            BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
            BooleanSupplier isValid, Runnable invalidationListener) {
        Objects.requireNonNull(capability);
        Objects.requireNonNull(level);
        Objects.requireNonNull(isValid);
        Objects.requireNonNull(invalidationListener);
        return new BlockCapabilityCache<>(capability, level, pos.immutable(), context, isValid, invalidationListener);
    }

    private final BlockCapability<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;
    private final BooleanSupplier isValid;
    private final Runnable invalidationListener;

    private boolean cacheValid = false;
    @Nullable
    private T cachedCap = null;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level,
                                  BlockPos pos, C context,
                                  BooleanSupplier isValid, Runnable invalidationListener) {
        this.capability = capability;
        this.level = level;
        this.pos = pos;
        this.context = context;
        this.isValid = isValid;
        this.invalidationListener = invalidationListener;
    }

    public ServerLevel level() { return level; }
    public BlockPos pos() { return pos; }
    public C context() { return context; }

    /**
     * Gets the capability instance, or {@code null} if the capability is not present.
     *
     * <p>If the target position is not loaded, returns {@code null}.</p>
     */
    @Nullable
    public T getCapability() {
        if (!cacheValid) {
            if (!level.isLoaded(pos)) {
                cachedCap = null;
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                cachedCap = capability.getCapability(level, pos, null, be, context);
            }
            cacheValid = true;
        }
        return cachedCap;
    }

    /**
     * Invalidates the cached capability. Next call to {@link #getCapability()} will re-query.
     */
    public void invalidate() {
        if (cacheValid) {
            cacheValid = false;
            cachedCap = null;
            if (isValid.getAsBoolean()) {
                invalidationListener.run();
            }
        }
    }
}
