package io.jacksoon.router.connection.store;

import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.RouteRuleSnapshot;
import io.jacksoon.init.annotation.Init;

import java.util.Comparator;
import java.util.List;

@Init
public class RouterRegistryStore {
    private volatile List<RouteRuleSnapshot> routeRules = List.of();

    public void save(RegistrySnapshot snapshot) {
        this.routeRules = snapshot.getRules()
                .stream()
                .sorted(Comparator.comparingInt(
                        (RouteRuleSnapshot rule) -> rule.getPathPrefix().length()
                ).reversed())
                .toList();
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