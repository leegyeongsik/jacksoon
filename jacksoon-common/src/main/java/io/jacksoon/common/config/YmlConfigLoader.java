package io.jacksoon.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class YmlConfigLoader {

    private static final String CONFIG_PROPERTY = "jacksoon.config";

    private final ObjectMapper objectMapper =
            new ObjectMapper(new YAMLFactory());

    public <T> T load(String resource, Class<T> type) {
        String externalConfig = System.getProperty(CONFIG_PROPERTY);
        if (externalConfig != null && !externalConfig.isBlank()) {
            return loadExternal(externalConfig, type);
        }
        return loadClasspath(resource, type);
    }

    private <T> T loadExternal(String configPath, Class<T> type) {
        Path path = Path.of(configPath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("external config not found: " + configPath);
        }
        try (InputStream inputStream = Files.newInputStream(path)) {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new IllegalStateException("failed to load external config: " + configPath, e);
        }
    }

    private <T> T loadClasspath(String resource, Class<T> type) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalStateException("config not found: " + resource);
            }
            return objectMapper.readValue(inputStream, type);

        } catch (IOException e) {
            throw new IllegalStateException("failed to load config: " + resource, e);
        }
    }
}