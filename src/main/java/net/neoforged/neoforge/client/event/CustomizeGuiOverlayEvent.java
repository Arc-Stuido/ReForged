package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphics;

/** Wrapper around Forge's {@link net.minecraftforge.client.event.CustomizeGuiOverlayEvent}. */
public class CustomizeGuiOverlayEvent {
    private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate;

    public CustomizeGuiOverlayEvent(net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate) {
        this.delegate = delegate;
    }

    public GuiGraphics getGuiGraphics() { return delegate.getGuiGraphics(); }

    public static class BossEventProgress extends CustomizeGuiOverlayEvent {
        private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress forgeEvent;

        public BossEventProgress(net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public net.minecraft.client.gui.components.LerpingBossEvent getBossEvent() {
            return forgeEvent.getBossEvent();
        }

        public int getX() { return forgeEvent.getX(); }
        public int getY() { return forgeEvent.getY(); }
        public int getIncrement() { return forgeEvent.getIncrement(); }
        public void setIncrement(int increment) { forgeEvent.setIncrement(increment); }
    }
}
