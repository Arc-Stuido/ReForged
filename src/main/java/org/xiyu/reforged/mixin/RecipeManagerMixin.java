package org.xiyu.reforged.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes recipe deserialization error-tolerant.
 *
 * <p>Vanilla/Forge only catches {@code IllegalArgumentException | JsonParseException}
 * in the recipe parsing loop. NeoForge mod codecs may throw other runtime exceptions
 * (e.g. IndexOutOfBoundsException from Create's SequencedRecipe) that escape the
 * catch block and crash the game.</p>
 *
 * <p>This mixin wraps the {@code Codec.parse()} call to convert any exception into
 * a {@code DataResult.error()}, which is then handled gracefully by the existing
 * error-handling code path.</p>
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    private static final Logger REFORGED_LOGGER = LogManager.getLogger("ReForged");

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
        ),
        remap = false
    )
    private DataResult reforged$safeRecipeParse(Codec codec, DynamicOps ops, Object input) {
        try {
            return codec.parse(ops, input);
        } catch (Exception e) {
            REFORGED_LOGGER.error("[ReForged] Recipe codec threw exception during parse: {}", e.getMessage());
            return DataResult.error(() -> "[ReForged] Recipe codec exception: " + e.getMessage());
        }
    }
}
