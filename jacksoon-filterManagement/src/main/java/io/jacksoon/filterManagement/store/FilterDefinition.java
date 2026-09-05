package io.jacksoon.filterManagement.store;

import io.jacksoon.common.filter.FilterConfigDto;

import java.nio.file.Path;

public record FilterDefinition(
        FilterConfigDto config,
        long artifactVersion,
        Path jarPath
) {
}
