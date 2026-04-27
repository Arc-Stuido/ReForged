package org.xiyu.reforged.mixin;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.xiyu.reforged.bridge.MenuOpenBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Adds the NeoForge-style {@code openMenu(MenuProvider, Consumer)} method
 * to {@link Player} that returns {@link OptionalInt}.
 *
 * <p>In NeoForge 1.21.1, Player has {@code openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)}
 * returning OptionalInt. In Forge 1.21, only {@code IForgeServerPlayer.openMenu(MenuProvider, Consumer<FriendlyByteBuf>)}
 * exists, returning void. This bridges the gap.</p>
 */
@Mixin(value = Player.class, remap = false)
public abstract class PlayerOpenMenuMixin {

    @Shadow
    public abstract OptionalInt openMenu(@Nullable MenuProvider provider);

    /**
     * NeoForge-style openMenu with extra data consumer.
     * Delegates to Forge's IForgeServerPlayer.openMenu for actual packet handling,
     * then returns the container ID.
     */
    public OptionalInt openMenu(@Nullable MenuProvider provider, @Nullable Consumer<?> extraDataWriter) {
        return MenuOpenBridge.openMenu((Player) (Object) this, provider, extraDataWriter);
    }
}
