package org.xiyu.reforged.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.reforged.bridge.ArgumentTypeBridge;

import java.util.Map;

/**
 * Allows NeoForge mod command argument types to resolve even when the registering
 * {@code Class} object differs from the runtime instance class across loaders.
 */
@Mixin(value = ArgumentTypeInfos.class, remap = false)
public class ArgumentTypeInfosMixin {

    @Inject(method = "byClass", at = @At("HEAD"), cancellable = true, remap = false)
    private static <A extends ArgumentType<?>> void reforged$byClassWithNameFallback(
            A argument,
            CallbackInfoReturnable<ArgumentTypeInfo<A, ?>> cir) {
        Map<Class<?>, ArgumentTypeInfo<?, ?>> byClass = ArgumentTypeInfosAccessorMixin.reforged$getByClass();
        ArgumentTypeInfo<A, ?> resolved = ArgumentTypeBridge.resolve(argument, byClass);
        if (resolved != null) {
            cir.setReturnValue(resolved);
            cir.cancel();
        }
    }

    @Inject(method = "isClassRecognized", at = @At("HEAD"), cancellable = true, remap = false)
    private static void reforged$isClassRecognizedWithNameFallback(
            Class<?> clazz,
            CallbackInfoReturnable<Boolean> cir) {
        Map<Class<?>, ArgumentTypeInfo<?, ?>> byClass = ArgumentTypeInfosAccessorMixin.reforged$getByClass();
        if (ArgumentTypeBridge.isRecognized(clazz, byClass)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
