package net.neoforged.neoforge.common;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;

/**
 * A basic implementation of VillagerTrades.ItemListing for use in trade events.
 */
public class BasicItemListing implements VillagerTrades.ItemListing {
    private final ItemStack price;
    @Nullable
    private final ItemStack price2;
    private final ItemStack forSale;
    private final int maxTrades;
    private final int xp;
    private final float priceMult;

    public BasicItemListing(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this.price = price;
        this.price2 = price2;
        this.forSale = forSale;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public BasicItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(new ItemStack(Items.EMERALD, emeralds), ItemStack.EMPTY, forSale, maxTrades, xp, priceMult);
    }

    public BasicItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp) {
        this(emeralds, forSale, maxTrades, xp, 0.05f);
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(net.minecraft.world.entity.Entity trader, net.minecraft.util.RandomSource rand) {
        return new MerchantOffer(
                new ItemCost(price.getItem(), price.getCount()),
                forSale.copy(),
                maxTrades, xp, priceMult
        );
    }
}
