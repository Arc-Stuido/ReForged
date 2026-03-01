package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;

public record DiscardedQueryAnswerPayload(
    @javax.annotation.Nullable FriendlyByteBuf data,
    java.util.function.Consumer<FriendlyByteBuf> encoder
) implements CustomQueryAnswerPayload {
    public static final DiscardedQueryAnswerPayload INSTANCE = new DiscardedQueryAnswerPayload();

    public DiscardedQueryAnswerPayload() {
        this(null, buf -> {});
    }

    @Override
    public void write(FriendlyByteBuf p_299186_) {
        if (this.data != null) {
            p_299186_.writeBytes(this.data.slice());
        } else {
            encoder.accept(p_299186_);
        }
    }
}
