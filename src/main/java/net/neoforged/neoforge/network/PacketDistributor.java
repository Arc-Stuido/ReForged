package net.neoforged.neoforge.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;
import org.slf4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Proxy: NeoForge's PacketDistributor — bridges to Forge's network distribution system.
 *
 * <p>Each static method maps to the corresponding NeoForge distribution pattern,
 * delegating to {@link PayloadChannelRegistry} for actual packet sending through
 * Forge's SimpleChannel infrastructure.</p>
 */
public final class PacketDistributor {
    private static final Logger LOGGER = LogUtils.getLogger();

    private PacketDistributor() {}

    /**
     * Send the given payload to the server (client → server).
     */
    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToServer: {}", payload.type().id());
        PayloadChannelRegistry.sendToServer(payload);
        for (CustomPacketPayload p : payloads) {
            PayloadChannelRegistry.sendToServer(p);
        }
    }

    /**
     * Send the given payload to the given player (server → client).
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayer: {} -> {}", payload.type().id(), player.getName().getString());
        PayloadChannelRegistry.sendToPlayer(player, payload);
        for (CustomPacketPayload p : payloads) {
            PayloadChannelRegistry.sendToPlayer(player, p);
        }
    }

    /**
     * Send the given payload to all players on the server.
     */
    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToAllPlayers: {}", payload.type().id());
        PayloadChannelRegistry.sendToAllPlayers(payload);
        for (CustomPacketPayload p : payloads) {
            PayloadChannelRegistry.sendToAllPlayers(p);
        }
    }

    /**
     * Send the given payload to all players in the given dimension.
     */
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayersInDimension: {}", payload.type().id());
        for (ServerPlayer player : level.players()) {
            PayloadChannelRegistry.sendToPlayer(player, payload);
            for (CustomPacketPayload p : payloads) {
                PayloadChannelRegistry.sendToPlayer(player, p);
            }
        }
    }

    /**
     * Send the given payload to all players near the given position.
     */
    public static void sendToPlayersNear(
            ServerLevel level,
            @Nullable ServerPlayer excluded,
            double x, double y, double z, double radius,
            CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayersNear: {}", payload.type().id());
        double r2 = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player == excluded) continue;
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            if (dx * dx + dy * dy + dz * dz < r2) {
                PayloadChannelRegistry.sendToPlayer(player, payload);
                for (CustomPacketPayload p : payloads) {
                    PayloadChannelRegistry.sendToPlayer(player, p);
                }
            }
        }
    }

    /**
     * Send the given payload to all players tracking the given entity.
     */
    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayersTrackingEntity: {}", payload.type().id());
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (var player : serverLevel.players()) {
                PayloadChannelRegistry.sendToPlayer(player, payload);
                for (CustomPacketPayload p : payloads) {
                    PayloadChannelRegistry.sendToPlayer(player, p);
                }
            }
        }
    }

    /**
     * Send the given payload to all players tracking the given entity and the entity itself if it is a player.
     */
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayersTrackingEntityAndSelf: {}", payload.type().id());
        sendToPlayersTrackingEntity(entity, payload, payloads);
        if (entity instanceof ServerPlayer self) {
            PayloadChannelRegistry.sendToPlayer(self, payload);
            for (CustomPacketPayload p : payloads) {
                PayloadChannelRegistry.sendToPlayer(self, p);
            }
        }
    }

    /**
     * Send the given payload to all players tracking the chunk at the given position.
     */
    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        LOGGER.debug("[ReForged] PacketDistributor.sendToPlayersTrackingChunk: {}", payload.type().id());
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            PayloadChannelRegistry.sendToPlayer(player, payload);
            for (CustomPacketPayload p : payloads) {
                PayloadChannelRegistry.sendToPlayer(player, p);
            }
        }
    }
}
