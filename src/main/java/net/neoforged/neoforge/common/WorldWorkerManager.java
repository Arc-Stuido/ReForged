package net.neoforged.neoforge.common;

/**
 * Stub: Background world work manager for tick-based chunked tasks.
 */
public class WorldWorkerManager {
    private WorldWorkerManager() {}

    public interface IWorker {
        boolean hasWork();
        boolean doWork();
    }

    public static void addWorker(IWorker worker) {
        // NO-OP in stub
    }
}
