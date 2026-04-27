package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;

/** Wrapper around Forge's {@link net.minecraftforge.client.event.CustomizeGuiOverlayEvent}. */
public class CustomizeGuiOverlayEvent {
    private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate;

    public CustomizeGuiOverlayEvent(net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate) {
        this.delegate = delegate;
    }

    public GuiGraphics getGuiGraphics() { return delegate.getGuiGraphics(); }
    public com.mojang.blaze3d.platform.Window getWindow() { return delegate.getWindow(); }
    public float getPartialTick() { return delegate.getPartialTick(); }

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

    /** Wrapper for Forge's DebugText event — adds/removes debug screen text. */
    public static class DebugText extends CustomizeGuiOverlayEvent {
        private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText forgeEvent;
        private final List<String> otherSideText = new ArrayList<>();

        public DebugText(net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public List<String> getText() { return forgeEvent.getText(); }

        public List<String> getLeft() {
            return isLeft() ? forgeEvent.getText() : otherSideText;
        }

        public List<String> getRight() {
            return isRight() ? forgeEvent.getText() : otherSideText;
        }

        public boolean isLeft() {
            return forgeEvent.getSide() == net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText.Side.Left;
        }

        public boolean isRight() {
            return forgeEvent.getSide() == net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText.Side.Right;
        }
    }
}
