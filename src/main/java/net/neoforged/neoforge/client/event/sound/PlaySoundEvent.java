package net.neoforged.neoforge.client.event.sound;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a sound is played.
 */
public class PlaySoundEvent extends Event {
    private final String name;

    public PlaySoundEvent(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
