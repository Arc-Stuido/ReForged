package net.neoforged.neoforge.client.model.geometry;

import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NeoForge's IUnbakedGeometry — extends Forge's equivalent.
 *
 * <p>NeoForge mods implement the NeoForge overloads of {@code bake()} and
 * {@code resolveParents()} that take NeoForge's {@link IGeometryBakingContext}.
 * The default bridge methods delegate Forge's parameter-type calls to NeoForge's,
 * adapting the context on the fly.</p>
 *
 * <p>The bridge bake() method wraps the call in a try-catch to gracefully handle
 * NoSuchMethodError and other errors from NeoForge-patched classes missing in Forge.</p>
 */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>>
        extends net.minecraftforge.client.model.geometry.IUnbakedGeometry<T> {

    Logger REFORGED_GEOMETRY_LOGGER = LogManager.getLogger("ReForged/Geometry");

    // ---- NeoForge abstract methods (what NeoForge mods implement) ----

    BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                    Function<Material, TextureAtlasSprite> spriteGetter,
                    ModelState modelState, ItemOverrides overrides);

    default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {}

    default Set<String> getConfigurableComponentNames() {
        return Set.of();
    }

    // ---- Bridge defaults: Forge descriptor → NeoForge descriptor ----

    @Override
    default BakedModel bake(net.minecraftforge.client.model.geometry.IGeometryBakingContext context,
                            ModelBaker baker,
                            Function<Material, TextureAtlasSprite> spriteGetter,
                            ModelState modelState, ItemOverrides overrides) {
        try {
            return bake(IGeometryBakingContext.adapt(context), baker, spriteGetter, modelState, overrides);
        } catch (Throwable t) {
            REFORGED_GEOMETRY_LOGGER.error("[ReForged] NeoForge geometry bake() failed for model '{}': {}",
                    context.getModelName(), t.getMessage(), t);
            // Return null — Forge's ModelBakery will treat this as a missing model
            return null;
        }
    }

    @Override
    default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                                net.minecraftforge.client.model.geometry.IGeometryBakingContext context) {
        try {
            resolveParents(modelGetter, IGeometryBakingContext.adapt(context));
        } catch (Throwable t) {
            REFORGED_GEOMETRY_LOGGER.error("[ReForged] NeoForge geometry resolveParents() failed for model '{}': {}",
                    context.getModelName(), t.getMessage(), t);
        }
    }
}
