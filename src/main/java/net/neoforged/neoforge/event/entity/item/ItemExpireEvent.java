package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * Stub: Fired when an ItemEntity expires (lifetime runs out).
 */
public class ItemExpireEvent extends ItemEvent {
    private int extraLife;

    public ItemExpireEvent(ItemEntity entityItem, int extraLife) {
        super(entityItem);
        this.extraLife = extraLife;
    }

    public int getExtraLife() {
        return extraLife;
    }

    public void setExtraLife(int extraLife) {
        this.extraLife = extraLife;
    }
}
