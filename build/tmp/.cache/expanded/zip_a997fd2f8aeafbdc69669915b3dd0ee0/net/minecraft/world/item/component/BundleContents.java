package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.apache.commons.lang3.math.Fraction;

public final class BundleContents implements TooltipComponent {
    public static final BundleContents EMPTY = new BundleContents(List.of());
    public static final Codec<BundleContents> CODEC = ItemStack.CODEC.listOf().xmap(BundleContents::new, p_332949_ -> p_332949_.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStack.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(BundleContents::new, p_328832_ -> p_328832_.items);
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(1, 16);
    private static final int NO_STACK_INDEX = -1;
    final List<ItemStack> items;
    final Fraction weight;

    BundleContents(List<ItemStack> p_331924_, Fraction p_333046_) {
        this.items = p_331924_;
        this.weight = p_333046_;
    }

    public BundleContents(List<ItemStack> p_334686_) {
        this(p_334686_, computeContentWeight(p_334686_));
    }

    private static Fraction computeContentWeight(List<ItemStack> p_336274_) {
        Fraction fraction = Fraction.ZERO;

        for (ItemStack itemstack : p_336274_) {
            fraction = fraction.add(getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
        }

        return fraction;
    }

    static Fraction getWeight(ItemStack p_334916_) {
        BundleContents bundlecontents = p_334916_.get(DataComponents.BUNDLE_CONTENTS);
        if (bundlecontents != null) {
            return BUNDLE_IN_BUNDLE_WEIGHT.add(bundlecontents.weight());
        } else {
            List<BeehiveBlockEntity.Occupant> list = p_334916_.getOrDefault(DataComponents.BEES, List.of());
            return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, p_334916_.getMaxStackSize());
        }
    }

    public ItemStack getItemUnsafe(int p_329557_) {
        return this.items.get(p_329557_);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> items() {
        return this.items;
    }

    public Iterable<ItemStack> itemsCopy() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int size() {
        return this.items.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public boolean equals(Object p_330764_) {
        if (this == p_330764_) {
            return true;
        } else {
            return !(p_330764_ instanceof BundleContents bundlecontents)
                ? false
                : this.weight.equals(bundlecontents.weight) && ItemStack.listMatches(this.items, bundlecontents.items);
        }
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    @Override
    public String toString() {
        return "BundleContents" + this.items;
    }

    public static class Mutable {
        private final List<ItemStack> items;
        private Fraction weight;

        public Mutable(BundleContents p_333063_) {
            this.items = new ArrayList<>(p_333063_.items);
            this.weight = p_333063_.weight;
        }

        public BundleContents.Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            return this;
        }

        private int findStackIndex(ItemStack p_328563_) {
            if (!p_328563_.isStackable()) {
                return -1;
            } else {
                for (int i = 0; i < this.items.size(); i++) {
                    if (ItemStack.isSameItemSameComponents(this.items.get(i), p_328563_)) {
                        return i;
                    }
                }

                return -1;
            }
        }

        private int getMaxAmountToAdd(ItemStack p_335684_) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);
            return Math.max(fraction.divideBy(BundleContents.getWeight(p_335684_)).intValue(), 0);
        }

        public int tryInsert(ItemStack p_333873_) {
            if (!p_333873_.isEmpty() && p_333873_.getItem().canFitInsideContainerItems()) {
                int i = Math.min(p_333873_.getCount(), this.getMaxAmountToAdd(p_333873_));
                if (i == 0) {
                    return 0;
                } else {
                    this.weight = this.weight.add(BundleContents.getWeight(p_333873_).multiplyBy(Fraction.getFraction(i, 1)));
                    int j = this.findStackIndex(p_333873_);
                    if (j != -1) {
                        ItemStack itemstack = this.items.remove(j);
                        ItemStack itemstack1 = itemstack.copyWithCount(itemstack.getCount() + i);
                        p_333873_.shrink(i);
                        this.items.add(0, itemstack1);
                    } else {
                        this.items.add(0, p_333873_.split(i));
                    }

                    return i;
                }
            } else {
                return 0;
            }
        }

        public int tryTransfer(Slot p_333053_, Player p_329130_) {
            ItemStack itemstack = p_333053_.getItem();
            int i = this.getMaxAmountToAdd(itemstack);
            return this.tryInsert(p_333053_.safeTake(itemstack.getCount(), i, p_329130_));
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            } else {
                ItemStack itemstack = this.items.remove(0).copy();
                this.weight = this.weight.subtract(BundleContents.getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                return itemstack;
            }
        }

        public Fraction weight() {
            return this.weight;
        }

        public BundleContents toImmutable() {
            return new BundleContents(List.copyOf(this.items), this.weight);
        }
    }
}