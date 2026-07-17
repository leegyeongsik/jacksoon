package io.jacksoon.console.service;

import io.jacksoon.console.dto.request.*;
import io.jacksoon.console.dto.response.FilterResponseDto;
import io.jacksoon.console.dto.response.ServiceResponseDto;
import io.jacksoon.console.entity.filter.FilterClass;
import io.jacksoon.console.entity.filter.FilterInfo;
import io.jacksoon.console.entity.filter.FilterMetric;
import io.jacksoon.console.entity.filter.FilterStage;
import io.jacksoon.console.entity.service.ServiceInstance;
import io.jacksoon.console.entity.service.ServiceMetric;
import io.jacksoon.console.entity.service.ServiceRule;
import io.jacksoon.console.entity.service.Services;
import io.jacksoon.console.repository.ConsoleRepository;
import io.jacksoon.console.repository.filter.FilterClassRepository;
import io.jacksoon.console.repository.filter.FilterInfoRepository;
import io.jacksoon.console.repository.filter.FilterMetricRepository;
import io.jacksoon.console.repository.filter.FilterStageRepository;
import io.jacksoon.console.repository.service.ServiceInstanceRepository;
import io.jacksoon.console.repository.service.ServiceMetricRepository;
import io.jacksoon.console.repository.service.ServiceRepository;
import io.jacksoon.console.repository.service.ServiceRuleRepository;
import io.jacksoon.console.type.FilterTiming;
import io.jacksoon.console.type.PipelineType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsoleService {
    private final ConsoleRepository consoleRepository;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final FilterClassRepository filterClassRepository;
    private final FilterInfoRepository filterInfoRepository;
    private final FilterMetricRepository filterMetricRepository;
    private final FilterStageRepository filterStageRepository;

    private final ServiceInstanceRepository serviceInstanceRepository;
    private final ServiceMetricRepository serviceMetricRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceRuleRepository serviceRuleRepository;
    @Value("${filter.server.url}")
    private String filterServerUrl;
    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getServices() {
        return consoleRepository.getServices();
    }

    @Transactional(readOnly = true)
    public List<FilterResponseDto> getFilters() {
        return consoleRepository.getFilters();
    }

    @Transactional
    public void registerService(ServiceProduceDto dto) {
        Services service = getOrCreateService(dto.getServiceName());
        List<InstanceProduceDto> instances = dto.getRegistryInstanceDtoList();
        for (InstanceProduceDto instanceDto : instances) {
            registerServiceInstance(service.getServiceId(), instanceDto);
        }
        replaceServiceRules(service.getServiceId(), dto.getRegistryRuleDtoList());
    }

    @Transactional
    public void removeService(ServiceProduceDto dto) {
        Services service = serviceRepository.findByServiceName(dto.getServiceName()).orElse(null);
        if (service == null) {
            return;
        }
        List<InstanceProduceDto> instances = dto.getRegistryInstanceDtoList();
        if (instances == null || instances.isEmpty()) {
            deleteService(service);
            return;
        }

        for (InstanceProduceDto instanceDto : instances) {
            serviceInstanceRepository.deleteByServiceIdAndInstanceId(service.getServiceId(), instanceDto.getInstanceId());
        }
        if (serviceInstanceRepository.countByServiceId(service.getServiceId()) == 0) {
            filterStageRepository.deleteById(service.getServiceId());
        }
    }

    @Transactional
    public void activateFilter(FilterProduceDto dto) {
        FilterInfo filterInfo = filterInfoRepository.findByFilterName(dto.getFilterName()).orElseThrow(IllegalStateException::new);
        filterInfo.updateActive(true);
    }

    @Transactional
    public void deleteFilter(FilterProduceDto dto) {
        FilterInfo filterInfo = filterInfoRepository.findByFilterName(dto.getFilterName()).orElse(null);
        if (filterInfo == null) {
            return;
        }
        Long filterInfoId = filterInfo.getFilterInfoId();
        Long filterStageId = filterInfo.getFilterStageId();
        filterMetricRepository.deleteAllByFilterInfoId(filterInfoId);
        filterClassRepository.deleteAllByFilterInfoId(filterInfoId);
        filterInfoRepository.delete(filterInfo);
        if (filterInfoRepository.countByFilterStageId(filterStageId) == 0) {
            filterStageRepository.deleteById(filterStageId);
        }
    }

    @Transactional
    public void saveServiceMetric(ServiceMetricProduceDto dto) {
        Services service = serviceRepository.findByServiceName(dto.getServiceName()).orElseThrow(IllegalStateException::new);
        ServiceMetric metric = new ServiceMetric(service.getServiceId(), dto.getSuccessCount(), dto.getFailureCount(), dto.getOccurredAt());
        serviceMetricRepository.save(metric);
    }

    @Transactional
    public void saveFilterMetric(FilterMetricProduceDto dto) {

        FilterInfo filterInfo = filterInfoRepository.findByFilterName(dto.getFilterName()).orElseThrow(IllegalStateException::new);
        FilterMetric metric = new FilterMetric(filterInfo.getFilterInfoId(), dto.getSuccessCount(), dto.getFailureCount(), dto.getOccurredAt());
        filterMetricRepository.save(metric);
    }

    @Transactional
    public void registerFilterClass(String filterName, PipelineType pipeline, FilterTiming timing, int order, MultipartFile sourceFile) {
        if (filterInfoRepository.existsByFilterName(filterName)) {
            throw new IllegalArgumentException();
        }
        String className = filterName;
        String sourceCode = readSourceCode(sourceFile);
        FilterStage stage = getOrCreateFilterStage(pipeline, timing);
        if (filterInfoRepository.existsByFilterStageIdAndOrder(stage.getFilterStageId(), order)) {
            throw new IllegalArgumentException();
        }

        FilterInfo filterInfo = filterInfoRepository.save(new FilterInfo(stage.getFilterStageId(), filterName, order));
        FilterClass filterClass = new FilterClass(filterInfo.getFilterInfoId(), className, sourceCode);
        filterClassRepository.save(filterClass);
        sendFilterClass(filterName,className,timing,pipeline,order,sourceFile);
    }

    private Services getOrCreateService(String serviceName) {
        return serviceRepository.findByServiceName(serviceName).orElseGet(() -> serviceRepository.save(new Services(serviceName)));
    }

    private void registerServiceInstance(Long serviceId, InstanceProduceDto dto) {
        if (serviceInstanceRepository.existsByServiceIdAndInstanceId(serviceId, dto.getInstanceId())) {
            return;
        }
        ServiceInstance instance = new ServiceInstance(serviceId, dto.getInstanceId(), dto.getHost(), dto.getPort(), dto.getProtocol(), dto.getHealthPath());
        serviceInstanceRepository.save(instance);
    }

    private void replaceServiceRules(Long serviceId, List<ServiceRuleProduceDto> ruleDto) {
        if (ruleDto == null) {
            return;
        }
        Set<String> pathPrefixes = new HashSet<>();
        List<ServiceRule> rules = ruleDto.stream().map(rule -> toServiceRule(serviceId, rule, pathPrefixes)).toList();
        serviceRuleRepository.deleteAllByServiceId(serviceId);
        if (!rules.isEmpty()) {
            serviceRuleRepository.saveAll(rules);
        }
    }

    private ServiceRule toServiceRule(Long serviceId, ServiceRuleProduceDto ruleDto, Set<String> pathPrefixes) {
        if (ruleDto == null) {
            throw new IllegalArgumentException();
        }
        if (!pathPrefixes.add(ruleDto.getPathPrefix())) {
            throw new IllegalArgumentException();
        }
        return new ServiceRule(serviceId, ruleDto.getPathPrefix(), ruleDto.isStripPrefix());
    }

    private void deleteService(Services service) {
        Long serviceId = service.getServiceId();
        serviceRuleRepository.deleteAllByServiceId(serviceId);
        serviceMetricRepository.deleteAllByServiceId(serviceId);
        serviceInstanceRepository.deleteAllByServiceId(serviceId);
        serviceRepository.delete(service);
    }

    private FilterStage getOrCreateFilterStage(PipelineType pipeline, FilterTiming timing) {
        return filterStageRepository.findByPipelineAndTiming(pipeline, timing).orElseGet(() -> filterStageRepository.save(new FilterStage(pipeline, timing)));
    }

    private String readSourceCode(MultipartFile sourceFile) {
        try {
            return new String(sourceFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException();
        }
    }

    public void sendFilterClass(String filterName, String className, FilterTiming timing, PipelineType pipeline, int order, MultipartFile classFile) {
        byte[] classBytes = readFile(classFile);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(filterServerUrl))
                .header("Content-Type", "application/octet-stream")
                .header("Filter-Name", filterName)
                .header("Class-Name", className)
                .header("Filter-Timing", timing.name())
                .header("Filter-Pipeline", pipeline.name())
                .header("Filter-Order", String.valueOf(order));
        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(classBytes)).build();
        sendRequest(request);
    }
    private byte[] readFile(MultipartFile sourceFile) {
        try {
            return  sourceFile.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException();
        }
    }
    private void sendRequest(HttpRequest request) {
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException();
            }
        } catch (IOException exception) {
            throw new IllegalStateException();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
    }
}