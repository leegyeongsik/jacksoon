package io.jacksoon.router.connection.store;

import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.RouteRuleSnapshot;
import io.jacksoon.common.registry.dto.response.ServiceSnapshot;
import io.jacksoon.init.annotation.Init;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Init
public class RouterRegistryStore {
    private volatile Map<String, ServiceSnapshot> serviceMap = Map.of();
    private volatile List<RouteRuleSnapshot> routeRules = List.of();

    public void save(RegistrySnapshot snapshot) {
        Map<String, ServiceSnapshot> nextServiceMap = snapshot.getServices().stream()
                .collect(Collectors.toUnmodifiableMap(
                        ServiceSnapshot::getServiceName,
                        Function.identity()
                ));
        List<RouteRuleSnapshot> nextRouteRules = snapshot.getRules()
                .stream()
                .sorted(Comparator.comparingInt(
                        (RouteRuleSnapshot rule) -> rule.getPathPrefix().length()
                ).reversed())
                .toList();

        this.serviceMap = nextServiceMap;
        this.routeRules = nextRouteRules;
    }

    public ResolvedRoute resolve(String path) {
        for (RouteRuleSnapshot rule : routeRules) {
            if (path.startsWith(rule.getPathPrefix())) {
                String backendPath = resolveBackendPath(path, rule);
                return new ResolvedRoute(rule.getServiceName(), backendPath);
            }
        }
        return null;
    }

    public ServiceSnapshot getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

    private String resolveBackendPath(String path, RouteRuleSnapshot rule) {
        if (!rule.isStripPrefix()) {
            return path;
        }

        String prefix = rule.getPathPrefix();
        String stripped = path.substring(prefix.length());

        if (stripped.isEmpty()) {
            return "/";
        }

        if (!stripped.startsWith("/")) {
            return "/" + stripped;
        }

        return stripped;
    }
}