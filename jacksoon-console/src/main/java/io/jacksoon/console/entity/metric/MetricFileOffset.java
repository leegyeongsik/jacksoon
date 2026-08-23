package io.jacksoon.console.entity.metric;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Entity
@Table(
        name = "metric_file_offset",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"metric_date", "file_name"})}
)
public class MetricFileOffset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "offset", nullable = false)
    private long offset;
    public MetricFileOffset(LocalDate metricDate, String fileName, long offset) {
        this.metricDate = metricDate;
        this.fileName = fileName;
        this.offset = offset;
    }
    public void updateOffset(long offset) {
        this.offset = offset;
    }
}
