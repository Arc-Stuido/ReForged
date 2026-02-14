package net.neoforged.neoforge.event.entity.living;

public class MobEffectEvent extends net.neoforged.bus.api.Event {

    public static class Applicable extends MobEffectEvent {
        public enum Result { DEFAULT, ACCEPT, DENY }
    }
}
