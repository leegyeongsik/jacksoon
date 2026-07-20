package io.jacksoon.common.filter;

public interface RouterFilter {
    default boolean isSupport(FilterContext context) {
        return true;
    }
    void doFilter(FilterContext context);
}