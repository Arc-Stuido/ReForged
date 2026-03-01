package net.neoforged.neoforge.event.brewing;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a brewing stand brews potions.
 */
public abstract class PotionBrewEvent extends Event {

    public static class Pre extends PotionBrewEvent {
        public Pre() {
        }
    }

    public static class Post extends PotionBrewEvent {
        public Post() {
        }
    }
}
