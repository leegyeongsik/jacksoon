package io.jacksoon.console.repository.filter;

import io.jacksoon.console.entity.filter.FilterClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilterClassRepository extends JpaRepository<FilterClass,Long> {
    void deleteAllByFilterInfoId(Long filterInfoId);
}
