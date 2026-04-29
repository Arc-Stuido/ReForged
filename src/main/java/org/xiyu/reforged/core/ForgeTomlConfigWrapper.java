package org.xiyu.reforged.core;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.forgespi.language.IConfigurable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Public equivalent of Forge's package-private NightConfigWrapper.
 */
public final class ForgeTomlConfigWrapper implements IConfigurable {

    private final UnmodifiableConfig config;

    public ForgeTomlConfigWrapper(UnmodifiableConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigElement(String... key) {
        try {
            return Optional.ofNullable((T) config.get(Arrays.asList(key)));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<? extends IConfigurable> getConfigList(String... key) {
        Optional<List<?>> values = getConfigElement(key);
        return values.orElse(List.of()).stream()
                .filter(UnmodifiableConfig.class::isInstance)
                .map(UnmodifiableConfig.class::cast)
                .map(ForgeTomlConfigWrapper::new)
                .toList();
    }
}
