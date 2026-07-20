package io.jacksoon.console.repository.filter;

import io.jacksoon.console.entity.filter.FilterInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilterInfoRepository extends JpaRepository<FilterInfo,Long> {
    Optional<FilterInfo> findByFilterName(String filterName);
    boolean existsByFilterName(String filterName);
    boolean existsByFilterStageIdAndOrder(Long filterStageId, int order);
    long countByFilterStageId(Long filterStageId);

}
