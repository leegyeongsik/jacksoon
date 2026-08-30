package io.jacksoon.registry.store;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.RouteRuleSnapshot;
import io.jacksoon.common.registry.dto.response.ServiceSnapshot;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.produce.RegistryAction;
import io.jacksoon.registry.dto.produce.RegistryProduceDto;
import io.jacksoon.registry.dto.produce.RegistryProduceRule;
import io.jacksoon.registry.dto.produce.RegistryRuleProduceDto;
import io.jacksoon.registry.dto.request.EndpointInfo;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.dto.request.RouteRule;
import io.jacksoon.registry.store.entity.RegisteredEndpoint;
import io.jacksoon.registry.store.entity.RegisteredRouteRule;
import io.jacksoon.registry.store.entity.RegisteredService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Init
public class RegistryStore {
    private final Map<String, RegisteredService> serviceMap = new ConcurrentHashMap<>();
    private final List<RegisteredRouteRule> routeRules = new ArrayList<>();
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;
    private long version;
    private long registrationSequence;

    public RegistryStore(CommonBlockingQueue<ProduceDto> produceDtoQueue) {
        this.produceDtoQueue = produceDtoQueue;
    }

    public synchronized void initialize(RegistrySnapshot snapshot) {
        serviceMap.clear();
        routeRules.clear();
        registrationSequence = 0L;

        List<ServiceSnapshot> services = snapshot.getServices() == null ? List.of() : snapshot.getServices();
        for (ServiceSnapshot serviceSnapshot : services) {
            RegisteredService service = new RegisteredService(serviceSnapshot.getServiceName());
            List<EndpointSnapshot> endpoints = serviceSnapshot.getEndpoints() == null ? List.of() : serviceSnapshot.getEndpoints();
            for (EndpointSnapshot endpoint : endpoints) {
                service.putEndpoint(new RegisteredEndpoint(
                        serviceSnapshot.getServiceName(),
                        endpoint.getInstanceId(),
                        endpoint.getHost(),
                        endpoint.getPort(),
                        endpoint.getProtocol(),
                        endpoint.getHealthPath(),
                        0L,
                        "active"
                ));
            }

            serviceMap.put(serviceSnapshot.getServiceName(), service);
        }

        List<RouteRuleSnapshot> rules = snapshot.getRules() == null ? List.of() : snapshot.getRules();

        for (RouteRuleSnapshot rule : rules) {
            routeRules.add(new RegisteredRouteRule(rule.getServiceName(), rule.getPathPrefix(), rule.isStripPrefix()));
        }
        version = snapshot.getVersion();
    }

    public synchronized long add(RegistryRegisterRequest request) {
        RegisteredService service = serviceMap.computeIfAbsent(request.getServiceName(), RegisteredService::new);
        long registrationId = ++registrationSequence;

        EndpointInfo endpoint = request.getEndpoint();
        RegisteredEndpoint registeredEndpoint = new RegisteredEndpoint(
                request.getServiceName(),
                request.getInstanceId(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getProtocol(),
                endpoint.getHealthPath(),
                registrationId,
                "pending"
        );

        RegisteredEndpoint previous = service.putEndpoint(registeredEndpoint);
        if (previous != null && "active".equals(previous.getStatus())) {
            long nextVersion = nextVersion();
            produceDtoQueue.put(new RegistryProduceDto(
                    RegistryAction.REMOVE,
                    previous,
                    "endpoint re-register pending",
                    nextVersion,
                    ProduceHint.SERVICE,
                    ProducerType.REGISTRY,
                    Instant.now()
            ));
        }

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            removeOldRules(request.getServiceName());
            List<RegistryRuleProduceDto> registeredRouteRules = new ArrayList<>();
            for (RouteRule rule : request.getRules()) {
                routeRules.add(new RegisteredRouteRule(request.getServiceName(), rule.getPathPrefix(), rule.isStripPrefix()));
                registeredRouteRules.add(new RegistryRuleProduceDto(rule.getPathPrefix(), rule.isStripPrefix()));
            }
            long nextVersion = nextVersion();
            produceDtoQueue.put(new RegistryProduceRule(request.getServiceName(), RegistryAction.REGISTER_RULE, registeredRouteRules, nextVersion));
        }
        return registrationId;
    }

    public synchronized RegistrySnapshot snapshot() {
        List<ServiceSnapshot> services = serviceMap.values().stream()
                .map(service -> {
                    List<EndpointSnapshot> endpoints = service.endpoints().stream()
                            .filter(endpoint -> "active".equals(endpoint.getStatus()))
                            .map(endpoint -> new EndpointSnapshot(
                                    endpoint.getInstanceId(),
                                    endpoint.getHost(),
                                    endpoint.getPort(),
                                    endpoint.getProtocol(),
                                    endpoint.getHealthPath()
                            ))
                            .toList();
                    return new ServiceSnapshot(service.getServiceName(), endpoints);
                })
                .toList();

        List<RouteRuleSnapshot> rules = routeRules.stream()
                .map(rule -> new RouteRuleSnapshot(rule.getServiceName(), rule.getPathPrefix(), rule.isStripPrefix()))
                .toList();
        return new RegistrySnapshot(version, services, rules);
    }

    public synchronized long version() {
        return version;
    }

    public synchronized void removeEndpoint(String serviceName, String instanceId, long registrationId) {
        RegisteredService service = serviceMap.get(serviceName);
        if (service == null) {
            return;
        }
        RegisteredEndpoint current = service.getEndpoint(instanceId);
        if (current == null || current.getRegistrationId() != registrationId) {
            return;
        }
        RegisteredEndpoint removed = service.removeEndpoint(instanceId);
        if (removed == null) {
            return;
        }
        boolean serviceRemoved = service.endpoints().isEmpty();
        if (serviceRemoved) {
            serviceMap.remove(serviceName);
            removeOldRules(serviceName);
        }
        if (!"active".equals(removed.getStatus()) && !serviceRemoved) {
            return;
        }
        long nextVersion = nextVersion();
        produceDtoQueue.put(new RegistryProduceDto(
                RegistryAction.REMOVE,
                removed,
                "unconnection-service",
                nextVersion,
                ProduceHint.SERVICE,
                ProducerType.REGISTRY,
                Instant.now()
        ));
    }

    public synchronized void successEndpoint(String serviceName, String instanceId, long registrationId) {
        RegisteredService service = serviceMap.get(serviceName);
        if (service == null) {
            return;
        }
        RegisteredEndpoint endpoint = service.getEndpoint(instanceId);
        if (endpoint == null || endpoint.getRegistrationId() != registrationId || "active".equals(endpoint.getStatus())) {
            return;
        }
        endpoint.setStatus("active");
        long nextVersion = nextVersion();
        produceDtoQueue.put(new RegistryProduceDto(
                RegistryAction.REGISTER,
                endpoint,
                "active-service",
                nextVersion,
                ProduceHint.SERVICE,
                ProducerType.REGISTRY,
                Instant.now()
        ));
    }

    private long nextVersion() {
        return ++version;
    }

    private void removeOldRules(String serviceName) {
        routeRules.removeIf(rule -> rule.getServiceName().equals(serviceName));
    }
}
