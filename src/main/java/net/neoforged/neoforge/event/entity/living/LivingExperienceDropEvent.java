package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingExperienceDropEvent}. */
public class LivingExperienceDropEvent {
    private final net.minecraftforge.event.entity.living.LivingExperienceDropEvent delegate;

    public LivingExperienceDropEvent(net.minecraftforge.event.entity.living.LivingExperienceDropEvent delegate) {
        this.delegate = delegate;
    }

    public LivingEntity getEntity() { return delegate.getEntity(); }
    public int getDroppedExperience() { return delegate.getDroppedExperience(); }
    public void setDroppedExperience(int xp) { delegate.setDroppedExperience(xp); }
    public Player getAttackingPlayer() { return delegate.getAttackingPlayer(); }
    public int getOriginalExperience() { return delegate.getOriginalExperience(); }
}
