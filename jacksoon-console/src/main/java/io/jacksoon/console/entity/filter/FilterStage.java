package io.jacksoon.console.entity.filter;

import io.jacksoon.console.type.FilterTiming;
import io.jacksoon.console.type.PipelineType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "filter_stages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_filter_stage_pipeline_timing",
                        columnNames = {
                                "pipeline",
                                "timing"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_stage_id")
    private Long filterStageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline", nullable = false)
    private PipelineType pipeline;

    @Enumerated(EnumType.STRING)
    @Column(name = "timing", nullable = false)
    private FilterTiming timing;

    public FilterStage(PipelineType pipeline, FilterTiming timing) {
        this.pipeline = pipeline;
        this.timing = timing;
    }
}