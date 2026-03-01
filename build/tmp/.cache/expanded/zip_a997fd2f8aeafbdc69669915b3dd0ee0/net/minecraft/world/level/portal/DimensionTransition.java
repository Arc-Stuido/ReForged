package net.minecraft.world.level.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record DimensionTransition(
    ServerLevel newLevel,
    Vec3 pos,
    Vec3 speed,
    float yRot,
    float xRot,
    boolean missingRespawnBlock,
    DimensionTransition.PostDimensionTransition postDimensionTransition
) {
    public static final DimensionTransition.PostDimensionTransition DO_NOTHING = p_343587_ -> {
    };
    public static final DimensionTransition.PostDimensionTransition PLAY_PORTAL_SOUND = DimensionTransition::playPortalSound;
    public static final DimensionTransition.PostDimensionTransition PLACE_PORTAL_TICKET = DimensionTransition::placePortalTicket;

    public DimensionTransition(
        ServerLevel p_343308_, Vec3 p_345120_, Vec3 p_344292_, float p_344085_, float p_342881_, DimensionTransition.PostDimensionTransition p_344117_
    ) {
        this(p_343308_, p_345120_, p_344292_, p_344085_, p_342881_, false, p_344117_);
    }

    public DimensionTransition(ServerLevel p_344161_, Entity p_342923_, DimensionTransition.PostDimensionTransition p_343140_) {
        this(p_344161_, findAdjustedSharedSpawnPos(p_344161_, p_342923_), Vec3.ZERO, 0.0F, 0.0F, false, p_343140_);
    }

    private static void playPortalSound(Entity p_342599_) {
        if (p_342599_ instanceof ServerPlayer serverplayer) {
            serverplayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        }
    }

    private static void placePortalTicket(Entity p_344820_) {
        p_344820_.placePortalTicket(BlockPos.containing(p_344820_.position()));
    }

    public static DimensionTransition missingRespawnBlock(ServerLevel p_344639_, Entity p_345092_, DimensionTransition.PostDimensionTransition p_342728_) {
        return new DimensionTransition(p_344639_, findAdjustedSharedSpawnPos(p_344639_, p_345092_), Vec3.ZERO, 0.0F, 0.0F, true, p_342728_);
    }

    private static Vec3 findAdjustedSharedSpawnPos(ServerLevel p_345512_, Entity p_344855_) {
        return p_344855_.adjustSpawnLocation(p_345512_, p_345512_.getSharedSpawnPos()).getBottomCenter();
    }

    @FunctionalInterface
    public interface PostDimensionTransition {
        void onTransition(Entity p_344450_);

        default DimensionTransition.PostDimensionTransition then(DimensionTransition.PostDimensionTransition p_343447_) {
            return p_344375_ -> {
                this.onTransition(p_344375_);
                p_343447_.onTransition(p_344375_);
            };
        }
    }
}