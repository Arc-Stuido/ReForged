package net.neoforged.neoforge.network;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Stub: Factory interface for creating container menus from network data.
 *
 * <p>NeoForge mods compile lambdas against the NeoForge signature
 * {@code create(int, Inventory, RegistryFriendlyByteBuf)}, so that overload must be
 * the single abstract method here. Forge's menu pipeline calls
 * {@code create(int, Inventory, FriendlyByteBuf)} (see Forge's patched
 * {@code MenuType.create}), which we bridge by wrapping the buffer.</p>
 *
 * <p>Extending Forge's {@code IContainerFactory} makes Forge's
 * {@code MenuType.create(id, inv, buf)} {@code instanceof} check pass, so the
 * extra data written by {@code NetworkHooks.openScreen} actually reaches the
 * NeoForge factory instead of being dropped.</p>
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu>
        extends net.minecraftforge.network.IContainerFactory<T> {

    T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);

    @Override
    default T create(int windowId, Inventory inv, FriendlyByteBuf data) {
        return create(windowId, inv, toRegistryBuf(data));
    }

    private static RegistryFriendlyByteBuf toRegistryBuf(FriendlyByteBuf data) {
        if (data == null) {
            return null;
        }
        if (data instanceof RegistryFriendlyByteBuf registryBuf) {
            return registryBuf;
        }
        return new RegistryFriendlyByteBuf(data, currentRegistryAccess());
    }

    private static RegistryAccess currentRegistryAccess() {
        try {
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                return server.registryAccess();
            }
        } catch (Throwable ignored) {
        }
        // Client side: menu packets are handled after a connection exists. Use
        // reflection so this interface never hard-references client-only classes.
        try {
            Class<?> mc = Class.forName("net.minecraft.client.Minecraft");
            Object instance = mc.getMethod("getInstance").invoke(null);
            Object connection = mc.getMethod("getConnection").invoke(instance);
            if (connection != null) {
                return (RegistryAccess) connection.getClass().getMethod("registryAccess").invoke(connection);
            }
        } catch (Throwable ignored) {
        }
        return RegistryAccess.EMPTY;
    }
}
