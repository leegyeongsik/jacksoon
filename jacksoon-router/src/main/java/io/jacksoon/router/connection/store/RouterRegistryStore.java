package io.jacksoon.router.connection.store;

import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.RouteRuleSnapshot;
import io.jacksoon.init.annotation.Init;

import java.util.Comparator;
import java.util.List;

@Init
public class RouterRegistryStore {
    private volatile long version = -1L;
    private volatile List<RouteRuleSnapshot> routeRules = List.of();

    public long version() {
        return version;
    }
    public void save(RegistrySnapshot snapshot) {
        List<RouteRuleSnapshot> rules = snapshot.getRules() == null ? List.of() : snapshot.getRules();
        this.routeRules = rules.stream().sorted(Comparator.comparingInt((RouteRuleSnapshot rule) -> rule.getPathPrefix().length()).reversed()).toList();
        this.version = snapshot.getVersion();
    }
    public ResolvedRoute resolve(String path) {
        for (RouteRuleSnapshot rule : routeRules) {
            if (matchesPrefix(path, rule.getPathPrefix())) {
                String backendPath = resolveBackendPath(path, rule);
                return new ResolvedRoute(rule.getServiceName(), backendPath);
            }
        }
        return null;
    }

    private boolean matchesPrefix(String path, String prefix) {
        if (path == null || prefix == null || prefix.isEmpty()) {
            return false;
        }
        if ("/".equals(prefix)) {
            return path.startsWith("/");
        }
        if (prefix.endsWith("/")) {
            return path.startsWith(prefix);
        }
        return path.equals(prefix) || path.startsWith(prefix + "/");
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