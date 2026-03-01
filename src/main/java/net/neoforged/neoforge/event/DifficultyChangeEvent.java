package net.neoforged.neoforge.event;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when the difficulty changes.
 */
public class DifficultyChangeEvent extends Event {
    private final net.minecraft.world.Difficulty oldDifficulty;
    private final net.minecraft.world.Difficulty newDifficulty;

    public DifficultyChangeEvent(net.minecraft.world.Difficulty newDifficulty, net.minecraft.world.Difficulty oldDifficulty) {
        this.newDifficulty = newDifficulty;
        this.oldDifficulty = oldDifficulty;
    }

    public net.minecraft.world.Difficulty getOldDifficulty() {
        return oldDifficulty;
    }

    public net.minecraft.world.Difficulty getDifficulty() {
        return newDifficulty;
    }
}
