package net.neoforged.neoforge.client.event;

public class ScreenEvent extends net.neoforged.bus.api.Event {

    public static class Closing extends ScreenEvent {
    }

    public static class Opening extends ScreenEvent {
    }

    public static class Init extends ScreenEvent {
        public static class Post extends Init {
        }
    }

    public static class Render extends ScreenEvent {
        public static class Post extends Render {
        }
    }
}
