package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerCommonPacketListener extends ServerCookiePacketListener, ServerPacketListener {
    void handleKeepAlive(ServerboundKeepAlivePacket p_300190_);

    void handlePong(ServerboundPongPacket p_297980_);

    void handleCustomPayload(ServerboundCustomPayloadPacket p_297952_);

    void handleResourcePackResponse(ServerboundResourcePackPacket p_300293_);

    void handleClientInformation(ServerboundClientInformationPacket p_301286_);
}