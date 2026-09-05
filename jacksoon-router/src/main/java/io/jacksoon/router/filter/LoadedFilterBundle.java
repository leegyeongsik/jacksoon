package io.jacksoon.router.filter;

import io.jacksoon.common.filter.FilterRegistryKey;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record LoadedFilterBundle(
        long version,
        Path bundlePath,
        URLClassLoader classLoader,
        Map<FilterRegistryKey, List<RegisteredFilter>> filters
) {
}