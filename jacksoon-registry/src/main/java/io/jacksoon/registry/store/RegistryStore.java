package io.jacksoon.registry.store;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.produce.RegistryAction;
import io.jacksoon.registry.dto.produce.RegistryProduceRule;
import io.jacksoon.registry.dto.produce.RegistryRuleProduceDto;
import io.jacksoon.registry.dto.request.EndpointInfo;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.dto.request.RouteRule;
import io.jacksoon.registry.dto.response.EndpointSnapshot;
import io.jacksoon.registry.dto.response.RegistrySnapshot;
import io.jacksoon.registry.dto.response.RouteRuleSnapshot;
import io.jacksoon.registry.dto.response.ServiceSnapshot;
import io.jacksoon.registry.store.entity.RegisteredEndpoint;
import io.jacksoon.registry.store.entity.RegisteredRouteRule;
import io.jacksoon.registry.store.entity.RegisteredService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Init
public class RegistryStore {
    private final Map<String, RegisteredService> serviceMap = new ConcurrentHashMap<>();
    private final List<RegisteredRouteRule> routeRules = new ArrayList<>();
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;

    public RegistryStore(CommonBlockingQueue<ProduceDto> produceDtoQueue) {
        this.produceDtoQueue = produceDtoQueue;
    }

    public synchronized void add(RegistryRegisterRequest request) {
        RegisteredService service = serviceMap.computeIfAbsent(request.getServiceName(), serviceName -> new RegisteredService(serviceName, produceDtoQueue));

        EndpointInfo endpoint = request.getEndpoint();

        RegisteredEndpoint registeredEndpoint = new RegisteredEndpoint(
                request.getServiceName(),
                request.getInstanceId(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getProtocol(),
                endpoint.getHealthPath(),
                "pending"
        );

        service.putEndpoint(registeredEndpoint);

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            removeOldRules(request.getServiceName());
            List<RegistryRuleProduceDto> registeredRouteRules = new ArrayList<>();
            for (RouteRule rule : request.getRules()) {
                RegisteredRouteRule registeredRouteRule = new RegisteredRouteRule(request.getServiceName(), rule.getPathPrefix(), rule.isStripPrefix());
                routeRules.add(registeredRouteRule);
                registeredRouteRules.add(new RegistryRuleProduceDto(rule.getPathPrefix(), rule.isStripPrefix()));
            }
            produceDtoQueue.put(new RegistryProduceRule(request.getServiceName(), RegistryAction.REGISTER_RULE, registeredRouteRules));
        }
    }

    private void removeOldRules(String serviceName) {
        routeRules.removeIf(rule -> rule.getServiceName().equals(serviceName));
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

        List<RouteRuleSnapshot> rules = routeRules.stream().map(rule -> new RouteRuleSnapshot(rule.getServiceName(), rule.getPathPrefix(), rule.isStripPrefix())).toList();

        return new RegistrySnapshot(services, rules);
    }

    public synchronized void removeEndpoint(String serviceName, String instanceId) {
        RegisteredService service = serviceMap.get(serviceName);
        if (service == null) {
            return;
        }
        service.removeEndpoint(instanceId);
        if (service.endpoints().isEmpty()) {
            serviceMap.remove(serviceName);
            removeOldRules(serviceName);
        }
    }

    public synchronized void successEndpoint(String serviceName, String instanceId) {
        RegisteredService service = serviceMap.get(serviceName);
        if (service == null) {
            return;
        }
        service.successEndPoint(instanceId);
    }
}