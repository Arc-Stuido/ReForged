package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Proxy: NeoForge's IAttachmentSerializer — serializer for data attachments.
 *
 * @param <S> The serialized tag type (e.g., Tag, CompoundTag)
 * @param <T> The attachment data type
 */
public interface IAttachmentSerializer<S, T> {
    /**
     * Read the attachment data from a serialized tag.
     */
    T read(IAttachmentHolder holder, S serializedData, HolderLookup.Provider provider);

    /**
     * Write the attachment data to a serialized tag.
     */
    @Nullable
    S write(T data, HolderLookup.Provider provider);
}
