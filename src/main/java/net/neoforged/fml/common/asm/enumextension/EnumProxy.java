package net.neoforged.fml.common.asm.enumextension;

import org.xiyu.reforged.core.EnumExtensionHandler;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Runtime handle for NeoForge enum-extension constants.
 */
public class EnumProxy<T extends Enum<T>> implements Supplier<T> {
    private static final Map<Class<?>, AtomicInteger> NEXT_INDEX = new ConcurrentHashMap<>();

    private final Class<T> enumClass;
    private final Object[] constructorArgs;
    private final int extensionIndex;
    private volatile T value;

    public EnumProxy(Class<T> enumClass, Object... constructorArgs) {
        this.enumClass = enumClass;
        this.constructorArgs = constructorArgs == null ? new Object[0] : constructorArgs.clone();
        this.extensionIndex = NEXT_INDEX.computeIfAbsent(enumClass, ignored -> new AtomicInteger()).getAndIncrement();
    }

    public T getValue() {
        T cached = value;
        if (cached != null) return cached;

        T resolved = resolveByExtensionOrder();
        if (resolved == null) {
            resolved = resolveByStringArgument();
        }
        if (resolved == null) {
            T[] constants = enumClass.getEnumConstants();
            if (constants != null && constants.length > 0) {
                resolved = constants[constants.length - 1];
            }
        }
        value = resolved;
        return resolved;
    }

    @Override
    public T get() {
        return getValue();
    }

    public Class<T> getEnumClass() {
        return enumClass;
    }

    public Object[] getConstructorArgs() {
        return constructorArgs.clone();
    }

    private T resolveByExtensionOrder() {
        List<String> names = EnumExtensionHandler.getExtendedEnumConstantNames(enumClass);
        if (extensionIndex >= names.size()) return null;
        try {
            return Enum.valueOf(enumClass, names.get(extensionIndex));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private T resolveByStringArgument() {
        for (Object arg : constructorArgs) {
            if (!(arg instanceof String raw)) continue;
            String token = raw;
            int colon = token.indexOf(':');
            if (colon >= 0) token = token.substring(colon + 1);
            token = token.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
            for (T constant : enumClass.getEnumConstants()) {
                if (constant.name().equals(token) || constant.name().endsWith("_" + token)) {
                    return constant;
                }
            }
        }
        return null;
    }
}
