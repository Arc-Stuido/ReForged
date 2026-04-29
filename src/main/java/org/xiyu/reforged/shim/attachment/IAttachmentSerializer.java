package org.xiyu.reforged.shim.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Shim for NeoForge's attachment serializer interface after package rewriting.
 */
public interface IAttachmentSerializer<S, T> {
    T read(IAttachmentHolder holder, S serializedData, HolderLookup.Provider provider);

    @Nullable
    S write(T data, HolderLookup.Provider provider);
}
