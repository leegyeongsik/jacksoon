package io.jacksoon.console.entity.filter;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "filter_classes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_filter_class_filter_info_id",
                        columnNames = "filter_info_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_class_id")
    private Long filterClassId;

    @Column(name = "filter_info_id", nullable = false)
    private Long filterInfoId;

    @Column(
            name = "class_name",
            nullable = false,
            length = 255
    )
    private String className;

    @Column(
            name = "source_code",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String sourceCode;

    public FilterClass(Long filterInfoId, String className, String sourceCode) {
        this.filterInfoId = filterInfoId;
        this.className = className;
        this.sourceCode = sourceCode;
    }
}