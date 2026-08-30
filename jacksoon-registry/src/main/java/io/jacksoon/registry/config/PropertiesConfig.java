package io.jacksoon.registry.config;

import io.jacksoon.common.config.YmlConfigLoader;
import io.jacksoon.init.annotation.Init;

@Init
public class PropertiesConfig {

    @Init
    public YmlConfigLoader ymlConfigLoader() {
        return new YmlConfigLoader();
    }

    @Init
    public RegistryProperties registryProperties(YmlConfigLoader ymlConfigLoader) {
        return ymlConfigLoader.load("application.yml", RegistryProperties.class);
    }
}
