package net.neoforged.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's IGeometryBakingContext — standalone interface (does NOT extend Forge's).
 *
 * <p>In real NeoForge this has the same method set as Forge's IGeometryBakingContext,
 * but crucially {@link #getRenderType(ResourceLocation)} returns NeoForge's
 * {@link RenderTypeGroup} rather than Forge's. Since these are final records in
 * different packages they cannot be related via inheritance, so this interface
 * must be independent of Forge's.</p>
 *
 * <p>The static {@link #adapt} method wraps a Forge-only context as a NeoForge context
 * when needed by bridge default methods in {@link IUnbakedGeometry}.</p>
 */
public interface IGeometryBakingContext {

    String getModelName();
    boolean hasMaterial(String name);
    Material getMaterial(String name);
    boolean isGui3d();
    boolean useBlockLight();
    boolean useAmbientOcclusion();
    ItemTransforms getTransforms();
    Transformation getRootTransform();
    @Nullable ResourceLocation getRenderTypeHint();
    boolean isComponentVisible(String component, boolean fallback);

    /**
     * Get the render type group for the given name.
     * Default delegates to Forge's NamedRenderTypeManager and converts the result.
     */
    default RenderTypeGroup getRenderType(ResourceLocation name) {
        return RenderTypeGroup.fromForge(net.minecraftforge.client.NamedRenderTypeManager.get(name));
    }

    /**
     * Adapt a Forge {@code IGeometryBakingContext} to a NeoForge one.
     * Wraps every method call, converting Forge types to NeoForge types where needed.
     */
    static IGeometryBakingContext adapt(net.minecraftforge.client.model.geometry.IGeometryBakingContext forgeCtx) {
        return new IGeometryBakingContext() {
            @Override public String getModelName() { return forgeCtx.getModelName(); }
            @Override public boolean hasMaterial(String name) { return forgeCtx.hasMaterial(name); }
            @Override public Material getMaterial(String name) { return forgeCtx.getMaterial(name); }
            @Override public boolean isGui3d() { return forgeCtx.isGui3d(); }
            @Override public boolean useBlockLight() { return forgeCtx.useBlockLight(); }
            @Override public boolean useAmbientOcclusion() { return forgeCtx.useAmbientOcclusion(); }
            @Override public ItemTransforms getTransforms() { return forgeCtx.getTransforms(); }
            @Override public Transformation getRootTransform() { return forgeCtx.getRootTransform(); }
            @Override public @Nullable ResourceLocation getRenderTypeHint() { return forgeCtx.getRenderTypeHint(); }
            @Override public boolean isComponentVisible(String component, boolean fallback) { return forgeCtx.isComponentVisible(component, fallback); }
            @Override public RenderTypeGroup getRenderType(ResourceLocation name) {
                return RenderTypeGroup.fromForge(forgeCtx.getRenderType(name));
            }
        };
    }
}
