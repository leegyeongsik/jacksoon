package io.jacksoon.router.filter;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.common.filter.RouterFilter;

public record RegisteredFilter(
        FilterConfigDto config,
        RouterFilter filter
){}

