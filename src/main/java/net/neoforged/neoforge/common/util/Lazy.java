package net.neoforged.neoforge.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

/**
 * Proxy: NeoForge's Lazy.
 * <p>
 * Must be a CLASS (not interface) because NeoForge mods are compiled with
 * Methodref (invokestatic on a class), not InterfaceMethodref.
 */
public class Lazy<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private T instance;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(@NotNull Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> concurrentOf(@NotNull Supplier<T> supplier) {
        return new ConcurrentLazy<>(supplier);
    }

    @Nullable
    @Override
    public T get() {
        if (supplier != null) {
            instance = supplier.get();
            supplier = null;
        }
        return instance;
    }

    private static final class ConcurrentLazy<T> extends Lazy<T> {
        private volatile Object lock = new Object();
        private volatile Supplier<T> concurrentSupplier;
        private volatile T concurrentInstance;

        private ConcurrentLazy(Supplier<T> supplier) {
            super(null);
            this.concurrentSupplier = supplier;
        }

        @Nullable
        @Override
        public T get() {
            Object localLock = this.lock;
            if (concurrentSupplier != null) {
                synchronized (localLock) {
                    if (concurrentSupplier != null) {
                        concurrentInstance = concurrentSupplier.get();
                        concurrentSupplier = null;
                        this.lock = null;
                    }
                }
            }
            return concurrentInstance;
        }
    }
}
