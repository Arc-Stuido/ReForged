package net.neoforged.neoforge.client.event;

import java.io.File;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a screenshot is taken.
 */
public class ScreenshotEvent extends Event {
    private final File screenshotFile;
    private Component resultMessage;

    public ScreenshotEvent(File screenshotFile, Component resultMessage) {
        this.screenshotFile = screenshotFile;
        this.resultMessage = resultMessage;
    }

    public File getScreenshotFile() { return screenshotFile; }
    public Component getResultMessage() { return resultMessage; }
    public void setResultMessage(Component msg) { this.resultMessage = msg; }
}
