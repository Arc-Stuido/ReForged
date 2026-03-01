package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public VaultRenderer(BlockEntityRendererProvider.Context p_335617_) {
        this.itemRenderer = p_335617_.getItemRenderer();
    }

    public void render(VaultBlockEntity p_335871_, float p_335940_, PoseStack p_331267_, MultiBufferSource p_329108_, int p_330387_, int p_332341_) {
        if (VaultBlockEntity.Client.shouldDisplayActiveEffects(p_335871_.getSharedData())) {
            Level level = p_335871_.getLevel();
            if (level != null) {
                ItemStack itemstack = p_335871_.getSharedData().getDisplayItem();
                if (!itemstack.isEmpty()) {
                    this.random.setSeed((long)ItemEntityRenderer.getSeedForItemStack(itemstack));
                    VaultClientData vaultclientdata = p_335871_.getClientData();
                    renderItemInside(
                        p_335940_,
                        level,
                        p_331267_,
                        p_329108_,
                        p_330387_,
                        itemstack,
                        this.itemRenderer,
                        vaultclientdata.previousSpin(),
                        vaultclientdata.currentSpin(),
                        this.random
                    );
                }
            }
        }
    }

    public static void renderItemInside(
        float p_329214_,
        Level p_331832_,
        PoseStack p_335991_,
        MultiBufferSource p_328725_,
        int p_328881_,
        ItemStack p_331088_,
        ItemRenderer p_335970_,
        float p_327698_,
        float p_335880_,
        RandomSource p_332964_
    ) {
        p_335991_.pushPose();
        p_335991_.translate(0.5F, 0.4F, 0.5F);
        p_335991_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_329214_, p_327698_, p_335880_)));
        ItemEntityRenderer.renderMultipleFromCount(p_335970_, p_335991_, p_328725_, p_328881_, p_331088_, p_332964_, p_331832_);
        p_335991_.popPose();
    }
}