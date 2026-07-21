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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConsoleService {
    private static final long MAX_JAVA_SOURCE_SIZE = 1024L * 1024L;
    private static final Pattern FILTER_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9._-]+");
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*(\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\b");
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
        registerServiceInstance(service.getServiceId(), dto);
    }

    @Transactional
    public void removeService(ServiceProduceDto dto) {
        Services service = serviceRepository.findByServiceName(dto.getServiceName()).orElse(null);
        if (service == null) {
            return;
        }
        serviceInstanceRepository.deleteByServiceIdAndInstanceId(service.getServiceId(), dto.getInstanceId());
        if (serviceInstanceRepository.countByServiceId(service.getServiceId()) == 0) {
            deleteService(service);
        }
    }

    @Transactional
    public void replaceServiceRules(ServiceRuleProduceRequestDto dto) {
        Services service = getOrCreateService(dto.getServiceName());
        replaceServiceRules(service.getServiceId(), dto.getRegistryRuleDtoList());
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
    public void registerFilter(String filterName, String className, PipelineType pipeline, FilterTiming timing, int order, MultipartFile sourceFile) {
        String normalizedFilterName = requireFilterName(filterName);
        String normalizedClassName = requireClassName(className);
        validateJavaSourceFile(sourceFile, normalizedClassName);
        if (filterInfoRepository.existsByFilterName(normalizedFilterName)) {
            throw new IllegalArgumentException();
        }
        byte[] sourceBytes = readFile(sourceFile);
        String sourceCode = new String(sourceBytes, StandardCharsets.UTF_8);
        validateSourceDeclaration(sourceCode, normalizedClassName);

        FilterStage stage = getOrCreateFilterStage(pipeline, timing);
        if (filterInfoRepository.existsByFilterStageIdAndOrder(stage.getFilterStageId(), order)) {
            throw new IllegalArgumentException();
        }

        FilterInfo filterInfo = filterInfoRepository.save(new FilterInfo(stage.getFilterStageId(), normalizedFilterName, order));
        filterClassRepository.save(new FilterClass(filterInfo.getFilterInfoId(), normalizedClassName, sourceCode));
        sendJavaFilter(normalizedFilterName, normalizedClassName, timing, pipeline, order, sourceBytes);
    }
    private Services getOrCreateService(String serviceName) {
        return serviceRepository.findByServiceName(serviceName).orElseGet(() -> serviceRepository.save(new Services(serviceName)));
    }
    private void registerServiceInstance(Long serviceId, ServiceProduceDto dto) {
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
        serviceRuleRepository.flush();
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

    private String requireFilterName(String filterName) {
        if (filterName == null || filterName.isBlank()) {
            throw new IllegalArgumentException();
        }

        String normalized = filterName.trim();
        if (!FILTER_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException();
        }
        return normalized;
    }

    private String requireClassName(String className) {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException();
        }

        String normalized = className.trim();
        if (!CLASS_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException();
        }
        return normalized;
    }

    private void validateJavaSourceFile(MultipartFile sourceFile, String className) {
        if (sourceFile == null || sourceFile.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (sourceFile.getSize() > MAX_JAVA_SOURCE_SIZE) {
            throw new IllegalArgumentException();
        }

        String originalFilename = sourceFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".java")) {
            throw new IllegalArgumentException();
        }

        String simpleClassName = getSimpleClassName(className);
        if (!originalFilename.equals(simpleClassName + ".java")) {
            throw new IllegalArgumentException();
        }
    }

    private void validateSourceDeclaration(String sourceCode, String className) {
        String expectedPackage = getPackageName(className);
        String expectedSimpleClassName = getSimpleClassName(className);

        Matcher packageMatcher = PACKAGE_PATTERN.matcher(sourceCode);
        String actualPackage = packageMatcher.find() ? packageMatcher.group(1) : "";
        if (!expectedPackage.equals(actualPackage)) {
            throw new IllegalArgumentException();
        }

        Matcher classMatcher = CLASS_PATTERN.matcher(sourceCode);
        boolean classFound = false;
        while (classMatcher.find()) {
            if (expectedSimpleClassName.equals(classMatcher.group(1))) {
                classFound = true;
                break;
            }
        }
        if (!classFound) {
            throw new IllegalArgumentException();
        }
    }

    private String getPackageName(String className) {
        int separatorIndex = className.lastIndexOf('.');
        return separatorIndex < 0 ? "" : className.substring(0, separatorIndex);
    }

    private String getSimpleClassName(String className) {
        int separatorIndex = className.lastIndexOf('.');
        return separatorIndex < 0 ? className : className.substring(separatorIndex + 1);
    }

    private void sendJavaFilter(String filterName, String className, FilterTiming timing, PipelineType pipeline, int order, byte[] sourceBytes) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(filterServerUrl))
                .header("Content-Type", "text/x-java-source; charset=UTF-8")
                .header("Filter-Name", filterName)
                .header("Class-Name", className)
                .header("Filter-Timing", timing.name())
                .header("Filter-Pipeline", pipeline.name())
                .header("Filter-Order", String.valueOf(order))
                .header("Filter-File-Type", "JAVA")
                .header("Filter-File-Hash", calculateSha256(sourceBytes))
                .POST(HttpRequest.BodyPublishers.ofByteArray(sourceBytes))
                .build();
        sendRequest(request);
    }

    private String calculateSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalArgumentException();
        }
    }
    private byte[] readFile(MultipartFile sourceFile) {
        try {
            return  sourceFile.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException();
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