package org.xiyu.reforged.bridge;

import io.netty.buffer.Unpooled;
import java.util.OptionalInt;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkInitialization;
import net.minecraftforge.network.packets.OpenContainer;

/**
 * Opens NeoForge-style menus on Forge while preserving RegistryFriendlyByteBuf extra data.
 */
public final class MenuOpenBridge {
    private static final int MAX_EXTRA_DATA_BYTES = 32600;

    private MenuOpenBridge() {
    }

    public static OptionalInt openMenu(Player player, @Nullable MenuProvider provider,
                                       @Nullable Consumer<?> extraDataWriter) {
        if (provider == null) {
            return OptionalInt.empty();
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return player.openMenu(provider);
        }

        if (serverPlayer.level().isClientSide) {
            return OptionalInt.empty();
        }

        serverPlayer.doCloseContainer();
        serverPlayer.nextContainerCounter();
        int containerId = serverPlayer.containerCounter;

        FriendlyByteBuf additionalData = writeAdditionalData(serverPlayer, extraDataWriter);
        AbstractContainerMenu menu = provider.createMenu(containerId, serverPlayer.getInventory(), serverPlayer);
        if (menu == null) {
            additionalData.release();
            return OptionalInt.empty();
        }

        OpenContainer packet = new OpenContainer(menu.getType(), containerId,
                provider.getDisplayName(), additionalData);
        NetworkInitialization.PLAY.send(packet, serverPlayer.connection.getConnection());

        serverPlayer.containerMenu = menu;
        serverPlayer.initMenu(menu);
        ForgeEventFactory.onPlayerOpenContainer(serverPlayer, menu);
        return OptionalInt.of(containerId);
    }

    @SuppressWarnings("unchecked")
    private static FriendlyByteBuf writeAdditionalData(ServerPlayer player, @Nullable Consumer<?> extraDataWriter) {
        RegistryFriendlyByteBuf registryData =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        if (extraDataWriter != null) {
            ((Consumer<RegistryFriendlyByteBuf>) extraDataWriter).accept(registryData);
        }

        registryData.readerIndex(0);
        FriendlyByteBuf framedData = new FriendlyByteBuf(Unpooled.buffer());
        framedData.writeVarInt(registryData.readableBytes());
        framedData.writeBytes(registryData);
        framedData.readerIndex(0);

        int readable = framedData.readableBytes();
        if (readable > MAX_EXTRA_DATA_BYTES || readable < 1) {
            framedData.release();
            throw new IllegalArgumentException(
                    "Invalid PacketBuffer for openGui, found " + readable + " bytes");
        }
        return framedData;
    }
}
