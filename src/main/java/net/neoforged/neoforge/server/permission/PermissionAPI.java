package net.neoforged.neoforge.server.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.Nullable;

/**
 * Shim: NeoForge PermissionAPI — delegates to the node's default resolver.
 */
public final class PermissionAPI {
    private PermissionAPI() {}

    public static Collection<PermissionNode<?>> getRegisteredNodes() {
        return Collections.emptySet();
    }

    @Nullable
    public static net.minecraft.resources.ResourceLocation getActivePermissionHandler() {
        return null;
    }

    /**
     * Queries a player's permission for a given node.
     * Falls back to the node's default resolver since we cannot directly bridge
     * NeoForge PermissionNode to Forge's permission handler.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(player, player.getUUID(), context);
    }

    /**
     * Queries an offline player's permission for a given node.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(null, player, context);
    }

    /**
     * Stub: initialization hook — no-op in the shim.
     */
    public static void initializePermissionAPI() {
        // No-op: the Forge permission system is initialized by Forge itself.
    }
}
