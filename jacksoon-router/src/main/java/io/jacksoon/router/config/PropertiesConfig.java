package io.jacksoon.router.config;

import io.jacksoon.common.config.YmlConfigLoader;
import io.jacksoon.init.annotation.Init;

@Init
public class PropertiesConfig {

    @Init
    public YmlConfigLoader ymlConfigLoader() {
        return new YmlConfigLoader();
    }

    @Init
    public RouterProperties routerProperties(YmlConfigLoader ymlConfigLoader) {
        return ymlConfigLoader.load("application.yml", RouterProperties.class);
    }
}
