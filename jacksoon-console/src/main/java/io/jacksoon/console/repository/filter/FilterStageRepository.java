package io.jacksoon.console.repository.filter;

import io.jacksoon.console.entity.filter.FilterStage;
import io.jacksoon.console.type.FilterTiming;
import io.jacksoon.console.type.PipelineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilterStageRepository extends JpaRepository<FilterStage,Long> {
    Optional<FilterStage> findByPipelineAndTiming(PipelineType pipeline, FilterTiming timing);
}
