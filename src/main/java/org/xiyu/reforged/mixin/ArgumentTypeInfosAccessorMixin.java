package org.xiyu.reforged.mixin;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = ArgumentTypeInfos.class, remap = false)
public interface ArgumentTypeInfosAccessorMixin {

    @Accessor(value = "BY_CLASS", remap = false)
    static Map<Class<?>, ArgumentTypeInfo<?, ?>> reforged$getByClass() {
        throw new AssertionError();
    }
}
