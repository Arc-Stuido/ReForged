package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DiscardedQueryPayload(ResourceLocation id,
    @org.jetbrains.annotations.Nullable FriendlyByteBuf data,
    java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf> encoder
) implements CustomQueryPayload {
    public DiscardedQueryPayload(ResourceLocation id) {
        this(id, null, buf -> {});
    }

    @Override
    public void write(FriendlyByteBuf p_299949_) {
        if (this.data != null) {
            p_299949_.writeBytes(this.data.slice());
        } else {
            encoder.accept(p_299949_);
        }
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }
}
