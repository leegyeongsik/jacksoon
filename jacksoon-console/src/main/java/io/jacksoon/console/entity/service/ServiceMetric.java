package io.jacksoon.console.entity.service;

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
@Table(name = "service_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_metric_id")
    private Long serviceMetricId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "success_count", nullable = false)
    private long success;

    @Column(name = "failure_count", nullable = false)
    private long failure;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    public ServiceMetric(Long serviceId, long success, long failure, Instant measuredAt) {
        this.serviceId = serviceId;
        this.success = success;
        this.failure = failure;
        this.measuredAt = measuredAt;
    }
}