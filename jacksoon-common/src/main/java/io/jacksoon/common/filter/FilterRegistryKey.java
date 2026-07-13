package io.jacksoon.common.filter;

public record FilterRegistryKey(
        FilterTiming timing,
        PipelineType pipeline
) {
}