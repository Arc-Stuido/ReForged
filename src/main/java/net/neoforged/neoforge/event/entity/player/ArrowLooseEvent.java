package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stub: Fired when a bow's arrow is loosed. */
public class ArrowLooseEvent extends PlayerEvent {
    private final ItemStack bow;
    private final boolean hasAmmo;
    private int charge;

    public ArrowLooseEvent(Player player, ItemStack bow, int charge, boolean hasAmmo) {
        super();
        this.bow = bow;
        this.charge = charge;
        this.hasAmmo = hasAmmo;
    }

    public ItemStack getBow() { return bow; }
    public boolean hasAmmo() { return hasAmmo; }
    public int getCharge() { return charge; }
    public void setCharge(int charge) { this.charge = charge; }
}
