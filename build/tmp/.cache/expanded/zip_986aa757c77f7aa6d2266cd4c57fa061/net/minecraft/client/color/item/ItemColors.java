package net.minecraft.client.color.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.IdMapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemColors {
    private static final int DEFAULT = -1;
    // Forge: Use registry delegate as non-Vanilla item ids are not constant
    private final java.util.Map<net.minecraft.core.Holder.Reference<Item>, ItemColor> itemColors = new java.util.HashMap<>(32);

    public static ItemColors createDefault(BlockColors p_92684_) {
        ItemColors itemcolors = new ItemColors();
        itemcolors.register(
            (p_325299_, p_325300_) -> p_325300_ > 0 ? -1 : DyedItemColor.getOrDefault(p_325299_, -6265536),
            Items.LEATHER_HELMET,
            Items.LEATHER_CHESTPLATE,
            Items.LEATHER_LEGGINGS,
            Items.LEATHER_BOOTS,
            Items.LEATHER_HORSE_ARMOR
        );
        itemcolors.register((p_325301_, p_325302_) -> p_325302_ != 1 ? -1 : DyedItemColor.getOrDefault(p_325301_, 0), Items.WOLF_ARMOR);
        itemcolors.register((p_92705_, p_92706_) -> GrassColor.get(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        itemcolors.register((p_325303_, p_325304_) -> {
            if (p_325304_ != 1) {
                return -1;
            } else {
                FireworkExplosion fireworkexplosion = p_325303_.get(DataComponents.FIREWORK_EXPLOSION);
                IntList intlist = fireworkexplosion != null ? fireworkexplosion.colors() : IntList.of();
                int i = intlist.size();
                if (i == 0) {
                    return -7697782;
                } else if (i == 1) {
                    return FastColor.ARGB32.opaque(intlist.getInt(0));
                } else {
                    int j = 0;
                    int k = 0;
                    int l = 0;

                    for (int i1 = 0; i1 < i; i1++) {
                        int j1 = intlist.getInt(i1);
                        j += FastColor.ARGB32.red(j1);
                        k += FastColor.ARGB32.green(j1);
                        l += FastColor.ARGB32.blue(j1);
                    }

                    return FastColor.ARGB32.color(j / i, k / i, l / i);
                }
            }
        }, Items.FIREWORK_STAR);
        itemcolors.register(
            (p_325305_, p_325306_) -> p_325306_ > 0
                    ? -1
                    : FastColor.ARGB32.opaque(p_325305_.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor()),
            Items.POTION,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.TIPPED_ARROW
        );

        for (SpawnEggItem spawneggitem : SpawnEggItem.eggs()) {
            itemcolors.register((p_325308_, p_325309_) -> FastColor.ARGB32.opaque(spawneggitem.getColor(p_325309_)), spawneggitem);
        }

        itemcolors.register(
            (p_92687_, p_92688_) -> {
                BlockState blockstate = ((BlockItem)p_92687_.getItem()).getBlock().defaultBlockState();
                return p_92684_.getColor(blockstate, null, null, p_92688_);
            },
            Blocks.GRASS_BLOCK,
            Blocks.SHORT_GRASS,
            Blocks.FERN,
            Blocks.VINE,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.LILY_PAD
        );
        itemcolors.register((p_92696_, p_92697_) -> FoliageColor.getMangroveColor(), Blocks.MANGROVE_LEAVES);
        itemcolors.register(
            (p_325310_, p_325311_) -> p_325311_ == 0
                    ? -1
                    : FastColor.ARGB32.opaque(p_325310_.getOrDefault(DataComponents.MAP_COLOR, MapItemColor.DEFAULT).rgb()),
            Items.FILLED_MAP
        );
        net.minecraftforge.client.ForgeHooksClient.onItemColorsInit(itemcolors, p_92684_);
        return itemcolors;
    }

    public int getColor(ItemStack p_92677_, int p_92678_) {
        ItemColor itemcolor = this.itemColors.get(net.minecraftforge.registries.ForgeRegistries.ITEMS.getDelegateOrThrow(p_92677_.getItem()));
        return itemcolor == null ? -1 : itemcolor.getColor(p_92677_, p_92678_);
    }

    /** @deprecated Register via {@link net.minecraftforge.client.event.RegisterColorHandlersEvent.Item} */
    @Deprecated
    public void register(ItemColor p_92690_, ItemLike... p_92691_) {
        for (ItemLike itemlike : p_92691_) {
            this.itemColors.put(net.minecraftforge.registries.ForgeRegistries.ITEMS.getDelegateOrThrow(itemlike.asItem()), p_92690_);
        }
    }
}
