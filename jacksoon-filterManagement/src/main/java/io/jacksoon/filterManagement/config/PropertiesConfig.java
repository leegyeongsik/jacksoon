package io.jacksoon.filterManagement.config;

import io.jacksoon.common.config.YmlConfigLoader;
import io.jacksoon.init.annotation.Init;

@Init
public class PropertiesConfig {

    @Init
    public YmlConfigLoader ymlConfigLoader() {
        return new YmlConfigLoader();
    }

    @Init
    public FilterManagementProperties filterManagementProperties(YmlConfigLoader ymlConfigLoader) {
        return ymlConfigLoader.load("application.yml", FilterManagementProperties.class);
    }
}
