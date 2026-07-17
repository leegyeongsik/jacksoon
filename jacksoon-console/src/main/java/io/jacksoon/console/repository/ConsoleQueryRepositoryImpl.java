package io.jacksoon.console.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.jacksoon.console.dto.response.*;
import io.jacksoon.console.entity.filter.QFilterClass;
import io.jacksoon.console.entity.filter.QFilterInfo;
import io.jacksoon.console.entity.filter.QFilterMetric;
import io.jacksoon.console.entity.filter.QFilterStage;
import io.jacksoon.console.entity.service.QServiceInstance;
import io.jacksoon.console.entity.service.QServiceMetric;
import io.jacksoon.console.entity.service.QServiceRule;
import io.jacksoon.console.entity.service.QServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsoleQueryRepositoryImpl implements ConsoleRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ServiceResponseDto> getServices() {
        QServices service = QServices.services;
        QServiceInstance instance = QServiceInstance.serviceInstance;
        QServiceMetric metric = QServiceMetric.serviceMetric;
        QServiceRule rule = QServiceRule.serviceRule;

        List<Tuple> serviceRows = queryFactory
                .select(
                        service.serviceId,
                        service.serviceName
                )
                .from(service)
                .orderBy(service.serviceName.asc())
                .fetch();

        Map<Long, List<ServiceInstanceDto>> instancesByService = findInstancesByService(instance);

        Map<Long, MetricTotal> metricByService = findServiceMetricTotals(metric);

        Map<Long, List<ServiceRuleDto>> rulesByService = findRulesByService(rule);

        List<ServiceResponseDto> result = new ArrayList<>(serviceRows.size());

        for (Tuple row : serviceRows) {
            Long serviceId = row.get(service.serviceId);
            MetricTotal total = metricByService.getOrDefault(serviceId, MetricTotal.ZERO);
            result.add(new ServiceResponseDto(serviceId, row.get(service.serviceName), total.success(), total.failure(), instancesByService.getOrDefault(serviceId, List.of()), rulesByService.getOrDefault(serviceId, List.of())));
        }
        return result;
    }

    @Override
    public List<FilterResponseDto> getFilters() {
        QFilterStage stage = QFilterStage.filterStage;
        QFilterInfo filterInfo = QFilterInfo.filterInfo;
        QFilterClass filterClass = QFilterClass.filterClass;
        QFilterMetric metric = QFilterMetric.filterMetric;

        List<Tuple> stageRows = queryFactory
                .select(
                        stage.filterStageId,
                        stage.pipeline,
                        stage.timing
                )
                .from(stage)
                .orderBy(
                        stage.pipeline.asc(),
                        stage.timing.asc()
                )
                .fetch();

        Map<Long, String> classNameByFilterInfo = findClassNamesByFilterInfo(filterClass);

        Map<Long, MetricTotal> metricByFilterInfo = findFilterMetricTotals(metric);

        Map<Long, List<FilterInfoDto>> filtersByStage = findFiltersByStage(filterInfo, classNameByFilterInfo, metricByFilterInfo);

        List<FilterResponseDto> result = new ArrayList<>(stageRows.size());

        for (Tuple row : stageRows) {
            Long filterStageId = row.get(stage.filterStageId);
            result.add(new FilterResponseDto(filterStageId, row.get(stage.pipeline), row.get(stage.timing), filtersByStage.getOrDefault(filterStageId, List.of())));
        }
        return result;
    }

    private Map<Long, List<ServiceInstanceDto>> findInstancesByService(QServiceInstance instance) {
        List<Tuple> rows = queryFactory
                .select(
                        instance.serviceId,
                        instance.serviceInstanceId,
                        instance.instanceId,
                        instance.host,
                        instance.port,
                        instance.protocol,
                        instance.healthPath
                )
                .from(instance)
                .orderBy(
                        instance.serviceId.asc(),
                        instance.instanceId.asc()
                )
                .fetch();

        Map<Long, List<ServiceInstanceDto>> result = new LinkedHashMap<>();

        for (Tuple row : rows) {
            Long serviceId = row.get(instance.serviceId);
            result.computeIfAbsent(serviceId, ignored -> new ArrayList<>()).add(new ServiceInstanceDto(row.get(instance.serviceInstanceId), row.get(instance.instanceId), row.get(instance.host), row.get(instance.port), row.get(instance.protocol), row.get(instance.healthPath)));
        }
        return result;
    }

    private Map<Long, MetricTotal> findServiceMetricTotals(QServiceMetric metric) {
        NumberExpression<Long> successSum = metric.success.sum().coalesce(0L);
        NumberExpression<Long> failureSum = metric.failure.sum().coalesce(0L);
        List<Tuple> rows = queryFactory
                .select(
                        metric.serviceId,
                        successSum,
                        failureSum
                )
                .from(metric)
                .groupBy(metric.serviceId)
                .fetch();

        Map<Long, MetricTotal> result = new HashMap<>();

        for (Tuple row : rows) {
            result.put(row.get(metric.serviceId), new MetricTotal(valueOrZero(row.get(successSum)), valueOrZero(row.get(failureSum))));
        }
        return result;
    }

    private Map<Long, List<ServiceRuleDto>> findRulesByService(QServiceRule rule
    ) {
        List<Tuple> rows = queryFactory
                .select(
                        rule.serviceId,
                        rule.serviceRuleId,
                        rule.pathPrefix,
                        rule.stripPrefix
                )
                .from(rule)
                .orderBy(
                        rule.serviceId.asc(),
                        rule.serviceRuleId.asc()
                )
                .fetch();
        Map<Long, List<ServiceRuleDto>> result = new LinkedHashMap<>();
        for (Tuple row : rows) {
            Long serviceId = row.get(rule.serviceId);
            result.computeIfAbsent(serviceId, ignored -> new ArrayList<>()).add(new ServiceRuleDto(row.get(rule.serviceRuleId), row.get(rule.pathPrefix), Boolean.TRUE.equals(row.get(rule.stripPrefix))));
        }
        return result;
    }

    private Map<Long, String> findClassNamesByFilterInfo(QFilterClass filterClass) {
        List<Tuple> rows = queryFactory
                .select(
                        filterClass.filterInfoId,
                        filterClass.className
                )
                .from(filterClass)
                .fetch();

        Map<Long, String> result = new HashMap<>();

        for (Tuple row : rows) {
            result.put(row.get(filterClass.filterInfoId), row.get(filterClass.className));
        }

        return result;
    }

    private Map<Long, MetricTotal> findFilterMetricTotals(QFilterMetric metric) {
        NumberExpression<Long> successSum = metric.success.sum().coalesce(0L);
        NumberExpression<Long> failureSum = metric.failure.sum().coalesce(0L);
        List<Tuple> rows = queryFactory
                .select(
                        metric.filterInfoId,
                        successSum,
                        failureSum
                )
                .from(metric)
                .groupBy(metric.filterInfoId)
                .fetch();

        Map<Long, MetricTotal> result = new HashMap<>();

        for (Tuple row : rows) {
            result.put(row.get(metric.filterInfoId), new MetricTotal(valueOrZero(row.get(successSum)), valueOrZero(row.get(failureSum))));
        }
        return result;
    }

    private Map<Long, List<FilterInfoDto>> findFiltersByStage(QFilterInfo filterInfo, Map<Long, String> classNameByFilterInfo, Map<Long, MetricTotal> metricByFilterInfo) {
        List<Tuple> rows = queryFactory
                .select(
                        filterInfo.filterStageId,
                        filterInfo.filterInfoId,
                        filterInfo.filterName,
                        filterInfo.order,
                        filterInfo.active
                )
                .from(filterInfo)
                .orderBy(
                        filterInfo.filterStageId.asc(),
                        filterInfo.order.asc()
                )
                .fetch();

        Map<Long, List<FilterInfoDto>> result = new LinkedHashMap<>();

        for (Tuple row : rows) {
            Long filterStageId = row.get(filterInfo.filterStageId);
            Long filterInfoId = row.get(filterInfo.filterInfoId);
            MetricTotal total = metricByFilterInfo.getOrDefault(filterInfoId, MetricTotal.ZERO);
            result.computeIfAbsent(filterStageId, ignored -> new ArrayList<>()).add(new FilterInfoDto(filterInfoId, row.get(filterInfo.filterName), classNameByFilterInfo.get(filterInfoId), row.get(filterInfo.order), Boolean.TRUE.equals(row.get(filterInfo.active)), total.success(), total.failure()));
        }
        return result;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private record MetricTotal(long success, long failure) {
        private static final MetricTotal ZERO = new MetricTotal(0L, 0L);
    }
}
