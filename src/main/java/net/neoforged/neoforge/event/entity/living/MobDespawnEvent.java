package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Mob;

/**
 * Stub: Fired when a mob is about to despawn.
 */
public class MobDespawnEvent extends LivingEvent {
    public enum Result { DEFAULT, ALLOW, DENY }

    private Result result = Result.DEFAULT;

    public MobDespawnEvent(Mob entity) {
        super(entity);
    }

    public Result getMobDespawnResult() { return result; }
    public void setMobDespawnResult(Result result) { this.result = result; }
}
