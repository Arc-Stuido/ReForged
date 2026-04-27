package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.xiyu.reforged.bridge.MenuOpenBridge;

import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Stub extension interface for Player.
 */
public interface IPlayerExtension {

    default Player self() {
        return (Player) this;
    }

    default boolean isCloseEnough(Entity entity, double dist) {
        return self().distanceTo(entity) < dist;
    }

    default OptionalInt openMenu(MenuProvider provider, BlockPos pos) {
        return openMenu(provider, buf -> buf.writeBlockPos(pos));
    }

    default OptionalInt openMenu(MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return MenuOpenBridge.openMenu(self(), provider, extraDataWriter);
    }

    default boolean mayFly() {
        return self().getAbilities().mayfly;
    }

    default boolean isFakePlayer() {
        return false;
    }
}
