package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.reforged.shim.attachment.AttachmentHolder;
import org.xiyu.reforged.shim.attachment.AttachmentType;
import org.xiyu.reforged.shim.attachment.IAttachmentHolder;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Injects {@link AttachmentHolder.AsField} into Entity to support NeoForge's Data Attachment system.
 *
 * <p>This replaces the global static map approach with per-entity storage that is properly
 * garbage collected when the entity is removed.</p>
 */
@Mixin(Entity.class)
public abstract class EntityAttachmentHolderMixin implements IAttachmentHolder {

    @Unique
    private final AttachmentHolder.AsField reforged$attachmentHolder = new AttachmentHolder.AsField(this);

    @Override
    public <T> T getData(AttachmentType<T> type) {
        return reforged$attachmentHolder.getData(type);
    }

    @Override
    public <T> T setData(AttachmentType<T> type, T value) {
        return reforged$attachmentHolder.setData(type, value);
    }

    @Override
    public <T> boolean hasData(AttachmentType<T> type) {
        return reforged$attachmentHolder.hasData(type);
    }

    @Override
    public <T> T removeData(AttachmentType<T> type) {
        return reforged$attachmentHolder.removeData(type);
    }

    @Override
    public boolean hasAttachments() {
        return reforged$attachmentHolder.hasAttachments();
    }

    @Override
    public <T> T getData(Supplier<AttachmentType<T>> type) { return getData(type.get()); }
    @Override
    public <T> boolean hasData(Supplier<AttachmentType<T>> type) { return hasData(type.get()); }
    @Override
    public <T> T setData(Supplier<AttachmentType<T>> type, T value) { return setData(type.get(), value); }
    @Override
    public <T> T removeData(Supplier<AttachmentType<T>> type) { return removeData(type.get()); }
    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return hasData(type) ? Optional.of(getData(type)) : Optional.empty();
    }
    @Override
    public <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return getExistingData(type.get());
    }
}
