package org.xiyu.reforged.bridge;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Bridge for {@link BakedModel} method signature differences between NeoForge and Forge.
 *
 * <p>NeoForge adds {@code useAmbientOcclusion(BlockState, ModelData, RenderType) → TriState}.
 * Forge's {@link net.minecraftforge.client.extensions.IForgeBakedModel IForgeBakedModel} only has
 * {@code useAmbientOcclusion(BlockState, RenderType) → boolean}.
 *
 * <p>Called from bytecode rewritten by {@link org.xiyu.reforged.asm.BytecodeRewriter}.
 */
public final class BakedModelBridge {

    private BakedModelBridge() {}

    /**
     * Redirect target for NeoForge's
     * {@code BakedModel.useAmbientOcclusion(BlockState, ModelData, RenderType) → TriState}.
     *
     * <p>Delegates to Forge's {@code useAmbientOcclusion(BlockState, RenderType) → boolean}
     * (from IForgeBakedModel). If the model opts out of AO, returns {@link TriState#FALSE};
     * otherwise returns {@link TriState#DEFAULT} to let the standard AO logic decide
     * based on global settings and light emission.</p>
     */
    public static TriState useAmbientOcclusion(BakedModel model, BlockState state,
                                                ModelData data, RenderType renderType) {
        return model.useAmbientOcclusion(state, renderType) ? TriState.DEFAULT : TriState.FALSE;
    }
}
