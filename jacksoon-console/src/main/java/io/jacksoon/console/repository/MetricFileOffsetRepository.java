package io.jacksoon.console.repository;

import io.jacksoon.console.entity.metric.MetricFileOffset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MetricFileOffsetRepository extends JpaRepository<MetricFileOffset, Long> {
    Optional<MetricFileOffset> findByMetricDateAndFileName(LocalDate metricDate, String fileName);
}