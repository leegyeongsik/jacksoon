package io.jacksoon.common.filter;

import java.util.List;

public record FilterBundleMetadata(
        long version,
        List<FilterConfigDto> filters
) {
    public FilterBundleMetadata {
        filters = filters == null ? List.of() : List.copyOf(filters);
    }
}
