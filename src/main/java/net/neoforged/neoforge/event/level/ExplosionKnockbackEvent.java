package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an explosion applies knockback.
 */
public class ExplosionKnockbackEvent extends Event {
    private final Level level;

    public ExplosionKnockbackEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() { return level; }
}
