package io.jacksoon.router.pipeline.context;

import java.util.List;

public record DetachedContexts(ProxyContext current, List<ProxyContext> pending) {
}
