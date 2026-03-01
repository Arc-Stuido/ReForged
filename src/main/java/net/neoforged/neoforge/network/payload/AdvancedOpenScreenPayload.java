package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Stub: Advanced open screen payload for server → client screen opening.
 */
public record AdvancedOpenScreenPayload(int windowId, ResourceLocation menuType, net.minecraft.network.chat.Component title, FriendlyByteBuf additionalData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdvancedOpenScreenPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "open_screen"));

    @Override
    public CustomPacketPayload.Type<AdvancedOpenScreenPayload> type() {
        return TYPE;
    }
}
