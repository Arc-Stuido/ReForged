package net.neoforged.neoforge.common.util;

import net.minecraft.network.FriendlyByteBuf;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Stub: FriendlyByteBuf utility methods.
 */
public class FriendlyByteBufUtil {
    private FriendlyByteBufUtil() {}

    public static <T> void writeCollection(FriendlyByteBuf buf, Collection<T> collection, BiConsumer<FriendlyByteBuf, T> writer) {
        buf.writeVarInt(collection.size());
        for (T t : collection) {
            writer.accept(buf, t);
        }
    }

    public static <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
        int size = buf.readVarInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }
}
