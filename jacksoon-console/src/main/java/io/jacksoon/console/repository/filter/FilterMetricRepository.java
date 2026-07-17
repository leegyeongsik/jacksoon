package io.jacksoon.console.repository.filter;

import io.jacksoon.console.entity.filter.FilterMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterMetricRepository extends JpaRepository<FilterMetric,Long> {
    void deleteAllByFilterInfoId(Long filterInfoId);
}
