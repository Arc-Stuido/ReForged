package net.neoforged.neoforge.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

/**
 * Holds extra data that may be injected into a face.
 *
 * @param color            Color in ARGB format
 * @param blockLight       Block Light for this face from 0-15 (inclusive)
 * @param skyLight         Sky Light for this face from 0-15 (inclusive)
 * @param ambientOcclusion If this face has AO
 */
public record ExtraFaceData(int color, int blockLight, int skyLight, boolean ambientOcclusion) {

    public static final ExtraFaceData DEFAULT = new ExtraFaceData(0xFFFFFFFF, 0, 0, true);

    public static final Codec<Integer> COLOR = Codec.either(Codec.INT, Codec.STRING).xmap(
            either -> either.map(Function.identity(), str -> (int) Long.parseLong(str, 16)),
            color -> Either.right(Integer.toHexString(color)));

    public static final Codec<ExtraFaceData> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            COLOR.optionalFieldOf("color", 0xFFFFFFFF).forGetter(ExtraFaceData::color),
                            Codec.intRange(0, 15).optionalFieldOf("block_light", 0).forGetter(ExtraFaceData::blockLight),
                            Codec.intRange(0, 15).optionalFieldOf("sky_light", 0).forGetter(ExtraFaceData::skyLight),
                            Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(ExtraFaceData::ambientOcclusion))
                    .apply(builder, ExtraFaceData::new));

    @Nullable
    public static ExtraFaceData read(@Nullable JsonElement obj, @Nullable ExtraFaceData fallback) throws JsonParseException {
        if (obj == null) {
            return fallback;
        }
        return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(JsonParseException::new);
    }
}
