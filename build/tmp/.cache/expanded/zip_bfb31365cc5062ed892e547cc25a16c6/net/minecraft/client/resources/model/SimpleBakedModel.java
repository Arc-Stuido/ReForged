package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBakedModel implements BakedModel {
    protected final List<BakedQuad> unculledFaces;
    protected final Map<Direction, List<BakedQuad>> culledFaces;
    protected final boolean hasAmbientOcclusion;
    protected final boolean isGui3d;
    protected final boolean usesBlockLight;
    protected final TextureAtlasSprite particleIcon;
    protected final ItemTransforms transforms;
    protected final ItemOverrides overrides;
    /** Forge: Block render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FANCY fancy graphics} */
    protected final net.minecraftforge.client.ChunkRenderTypeSet blockRenderTypes;
    /** Forge: Block render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics} */
    protected final net.minecraftforge.client.ChunkRenderTypeSet blockRenderTypesFast;
    /** Forge: Item render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FANCY fancy graphics} */
    protected final List<net.minecraft.client.renderer.RenderType> itemRenderTypes;
    /** Forge: Item render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics} */
    protected final List<net.minecraft.client.renderer.RenderType> itemRenderTypesFast;
    /** Forge: Item render types to be used with {@linkplain net.minecraft.client.GraphicsStatus#FABULOUS fabulous graphics} */
    protected final List<net.minecraft.client.renderer.RenderType> fabulousItemRenderTypes;
    /** Forge: If this model's {@linkplain #blockRenderTypes block render types} are rendering cutout, to account for older leaves model JSONs */
    protected final boolean isRenderingCutout;

    /** @deprecated Forge: Use {@linkplain #SimpleBakedModel(List, Map, boolean, boolean, boolean, TextureAtlasSprite, ItemTransforms, ItemOverrides, net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup) variant with RenderTypeGroup} **/
    @Deprecated
    public SimpleBakedModel(List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_, ItemOverrides p_119496_) {
        this(p_119489_, p_119490_, p_119491_, p_119492_, p_119493_, p_119494_, p_119495_, p_119496_, net.minecraftforge.client.RenderTypeGroup.EMPTY);
    }

    /** @deprecated Forge: Use {@linkplain #SimpleBakedModel(List, Map, boolean, boolean, boolean, TextureAtlasSprite, ItemTransforms, ItemOverrides, net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup) variant with RenderTypeGroup for fast graphics} **/
    @Deprecated(forRemoval = true, since = "1.21.4")
    public SimpleBakedModel(List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_, ItemOverrides p_119496_, net.minecraftforge.client.RenderTypeGroup renderTypes) {
        this(p_119489_, p_119490_, p_119491_, p_119492_, p_119493_, p_119494_, p_119495_, p_119496_, renderTypes, net.minecraftforge.client.RenderTypeGroup.EMPTY);
    }

    /** Constructor with {@link net.minecraftforge.client.RenderTypeGroup RenderTypeGroup} for fancy and fast graphics. Preferred over {@link net.minecraft.client.renderer.ItemBlockRenderTypes#setRenderLayer(net.minecraft.world.level.block.Block, net.minecraft.client.renderer.RenderType) ItemBlockRenderTypes.setRenderLayer(Block, RenderType)}. */
    public SimpleBakedModel(
        List<BakedQuad> p_119489_,
        Map<Direction, List<BakedQuad>> p_119490_,
        boolean p_119491_,
        boolean p_119492_,
        boolean p_119493_,
        TextureAtlasSprite p_119494_,
        ItemTransforms p_119495_,
        ItemOverrides p_119496_,
        net.minecraftforge.client.RenderTypeGroup renderTypes,
        net.minecraftforge.client.RenderTypeGroup renderTypesFast
    ) {
        this.unculledFaces = p_119489_;
        this.culledFaces = p_119490_;
        this.hasAmbientOcclusion = p_119491_;
        this.isGui3d = p_119493_;
        this.usesBlockLight = p_119492_;
        this.particleIcon = p_119494_;
        this.transforms = p_119495_;
        this.overrides = p_119496_;

        boolean hasRenderTypes = renderTypes != null && !renderTypes.isEmpty();
        boolean hasRenderTypesFast = renderTypesFast != null && !renderTypesFast.isEmpty();
        this.blockRenderTypes = hasRenderTypes ? net.minecraftforge.client.ChunkRenderTypeSet.of(renderTypes.block()) : null;
        this.blockRenderTypesFast = hasRenderTypesFast ? net.minecraftforge.client.ChunkRenderTypeSet.of(renderTypesFast.block()) : null;
        this.itemRenderTypes = hasRenderTypes ? List.of(renderTypes.entity()) : null;
        this.itemRenderTypesFast = hasRenderTypesFast ? List.of(renderTypesFast.entity()) : null;
        this.fabulousItemRenderTypes = hasRenderTypes ? List.of(renderTypes.entityFabulous()) : null;
        this.isRenderingCutout = hasRenderTypes && (renderTypes.block() == net.minecraft.client.renderer.RenderType.cutout() || renderTypes.block() == net.minecraft.client.renderer.RenderType.cutoutMipped());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_235054_, @Nullable Direction p_235055_, RandomSource p_235056_) {
        return p_235055_ == null ? this.unculledFaces : this.culledFaces.get(p_235055_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return this.isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return this.usesBlockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleIcon;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private static final net.minecraftforge.client.ChunkRenderTypeSet SOLID_BLOCK = net.minecraftforge.client.ChunkRenderTypeSet.of(net.minecraft.client.renderer.RenderType.solid());
    private static final List<net.minecraft.client.renderer.RenderType> SOLID_BLOCK_ITEM = List.of(net.minecraft.client.renderer.RenderType.solid());

    @Override
    public net.minecraftforge.client.ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, net.minecraftforge.client.model.data.ModelData data) {
        if (!net.minecraft.client.renderer.ItemBlockRenderTypes.isFancy()) {
            if (blockRenderTypesFast != null)
                return blockRenderTypesFast;
            if (isRenderingCutout && state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
                return SOLID_BLOCK;
        }
        return blockRenderTypes != null ? blockRenderTypes : BakedModel.super.getRenderTypes(state, rand, data);
    }

    @Override
    public List<net.minecraft.client.renderer.RenderType> getRenderTypes(net.minecraft.world.item.ItemStack itemStack, boolean fabulous) {
        if (!fabulous) {
            if (!net.minecraft.client.renderer.ItemBlockRenderTypes.isFancy()) {
                if (itemRenderTypesFast != null)
                    return itemRenderTypesFast;
                if (isRenderingCutout && itemStack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem && blockItem.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
                    return SOLID_BLOCK_ITEM;
            }
            if (itemRenderTypes != null)
                return itemRenderTypes;
        } else {
            if (fabulousItemRenderTypes != null)
                return fabulousItemRenderTypes;
        }
        return BakedModel.super.getRenderTypes(itemStack, fabulous);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final List<BakedQuad> unculledFaces = Lists.newArrayList();
        private final Map<Direction, List<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final ItemOverrides overrides;
        private final boolean hasAmbientOcclusion;
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(BlockModel p_119517_, ItemOverrides p_119518_, boolean p_119519_) {
            this(p_119517_.hasAmbientOcclusion(), p_119517_.getGuiLight().lightLikeBlock(), p_119519_, p_119517_.getTransforms(), p_119518_);
        }

        public Builder(boolean p_119521_, boolean p_119522_, boolean p_119523_, ItemTransforms p_119524_, ItemOverrides p_119525_) {
            for (Direction direction : Direction.values()) {
                this.culledFaces.put(direction, Lists.newArrayList());
            }

            this.overrides = p_119525_;
            this.hasAmbientOcclusion = p_119521_;
            this.usesBlockLight = p_119522_;
            this.isGui3d = p_119523_;
            this.transforms = p_119524_;
        }

        public SimpleBakedModel.Builder addCulledFace(Direction p_119531_, BakedQuad p_119532_) {
            this.culledFaces.get(p_119531_).add(p_119532_);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad p_119527_) {
            this.unculledFaces.add(p_119527_);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite p_119529_) {
            this.particleIcon = p_119529_;
            return this;
        }

        public SimpleBakedModel.Builder item() {
            return this;
        }

        private net.minecraftforge.client.RenderTypeGroup renderTypes = net.minecraftforge.client.RenderTypeGroup.EMPTY;
        private net.minecraftforge.client.RenderTypeGroup renderTypesFast = net.minecraftforge.client.RenderTypeGroup.EMPTY;

        /**
         * Sets the render type to be used for this model, and will be used for any graphics setting.
         * <p>
         * If you need to set a specific render type for
         * {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics}, consider using
         * {@link #renderTypes(net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup)
         * renderTypes(RenderTypeGroup, RenderTypeGroup)} instead which allows choosing render types for both fancy and
         * fast graphics.
         *
         * @apiNote If this model is for {@linkplain net.minecraft.world.level.block.LeavesBlock leaves} and if the
         * given render type is either {@linkplain net.minecraft.client.renderer.RenderType#cutout() cutout} or
         * {@linkplain net.minecraft.client.renderer.RenderType#cutoutMipped() cutout mipped}, it will be overridden
         * with {@linkplain net.minecraft.client.renderer.RenderType#solid() solid} when fast graphics is enabled.
         * @see #renderTypes(net.minecraftforge.client.RenderTypeGroup, net.minecraftforge.client.RenderTypeGroup)
         * renderTypes(RenderTypeGroup, RenderTypeGroup)
         */
        public SimpleBakedModel.Builder renderTypes(net.minecraftforge.client.RenderTypeGroup renderTypes) {
            return this.renderTypes(renderTypes, net.minecraftforge.client.RenderTypeGroup.EMPTY);
        }

        /**
         * Sets the render types to be used for this model: one for
         * {@linkplain net.minecraft.client.GraphicsStatus#FANCY fancy graphics} and one for
         * {@linkplain net.minecraft.client.GraphicsStatus#FAST fast graphics}.
         *
         * @see #renderTypes(net.minecraftforge.client.RenderTypeGroup)
         */
        public SimpleBakedModel.Builder renderTypes(net.minecraftforge.client.RenderTypeGroup renderTypes, net.minecraftforge.client.RenderTypeGroup renderTypesFast) {
            this.renderTypes = renderTypes;
            this.renderTypesFast = renderTypesFast;
            return this;
        }

        public BakedModel build() {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                return new SimpleBakedModel(
                    // Forge: Account for render types in model JSONs
                    this.unculledFaces, this.culledFaces, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides, this.renderTypes, this.renderTypesFast
                );
            }
        }
    }
}
