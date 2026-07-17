package io.jacksoon.console.entity.filter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "filter_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_metric_id")
    private Long filterMetricId;

    @Column(name = "filter_info_id", nullable = false)
    private Long filterInfoId;

    @Column(name = "success_count", nullable = false)
    private long success;

    @Column(name = "failure_count", nullable = false)
    private long failure;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    public FilterMetric(Long filterInfoId, long success, long failure, Instant measuredAt) {
        this.filterInfoId = filterInfoId;
        this.success = success;
        this.failure = failure;
        this.measuredAt = measuredAt;
    }
}