package net.neoforged.neoforge.common.util;

import org.apache.logging.log4j.message.Message;

/**
 * Stub: Adapter to supply log messages lazily.
 */
public class LogMessageAdapter implements Message {
    private final java.util.function.Supplier<String> supplier;

    public LogMessageAdapter(java.util.function.Supplier<String> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String getFormattedMessage() {
        return supplier.get();
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
