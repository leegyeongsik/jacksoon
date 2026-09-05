package io.jacksoon.console.entity.filter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "filter_infos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_filter_info_name",
                        columnNames = "filter_name"
                ),
                @UniqueConstraint(
                        name = "uk_filter_info_stage_order",
                        columnNames = {
                                "filter_stage_id",
                                "filter_order"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_info_id")
    private Long filterInfoId;

    @Column(
            name = "filter_name",
            nullable = false,
            length = 100
    )
    private String filterName;

    @Column(name = "filter_stage_id", nullable = false)
    private Long filterStageId;

    @Column(name = "filter_order", nullable = false)
    private int order;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public FilterInfo(Long filterStageId, String filterName, int order) {
        this.filterStageId = filterStageId;
        this.filterName = filterName;
        this.order = order;
        this.active = false;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }
}