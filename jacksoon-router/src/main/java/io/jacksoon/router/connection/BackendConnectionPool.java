package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;
import io.jacksoon.router.exception.BackendConnectionException;
import io.jacksoon.router.exception.BackendUnavailableException;
import io.jacksoon.router.handler.BackendIOHandler;
import io.jacksoon.router.pipeline.context.ProxyContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class BackendConnectionPool {
    private volatile int totalPending;
    @Getter
    private final EndpointSnapshot endpoint;
    @Getter
    private final String serviceName;
    private final BackendConnectionFactory connectionFactory;
    private final int minConnection = 1;
    private final int maxConnection = 500;
    private final int growThreshold = 1;
    private final int shrinkThreshold = 0;
    private final long idleTimeoutMillis = 30_000L;
    private final Map<BackendIOHandler, Node> nodeMap = new IdentityHashMap<>();
    private final TreeSet<Node> nodes = new TreeSet<>(Comparator.comparingInt((Node node) -> node.pending).thenComparingLong(node -> node.order));
    private long nextOrder;
    private volatile boolean closed;

    public BackendConnectionPool(String serviceName, EndpointSnapshot endpoint, BackendConnectionFactory connectionFactory) {
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.connectionFactory = connectionFactory;
        init();
    }

    private synchronized void init() {
        for (int i = 0; i < minConnection; i++) {
            addConnection();
        }
    }

    public void send(ProxyContext context) {
        int attempts = 0;
        while (attempts++ < maxConnection) {
            Node node = reserve();
            boolean accepted = node.handler.send(context);
            if (accepted) {
                return;
            }
            rollback(node);
        }
        throw new BackendConnectionException("Failed to send request to backend connection. serviceName=" + serviceName);
    }

    private synchronized Node reserve() {
        if (closed) {
            throw new BackendUnavailableException(serviceName, "Backend connection pool is closed. serviceName=" + serviceName);
        }
        int attempts = 0;
        while (attempts++ < maxConnection) {
            if (nodes.isEmpty()) {
                addConnection();
            }
            growIfNeeded();
            if (nodes.isEmpty()) {
                break;
            }
            Node node = nodes.first();
            if (!node.handler.isAlive()) {
                removeNode(node);
                continue;
            }
            nodes.remove(node);
            node.pending++;
            node.order = nextOrder++;
            node.handler.increasePending();
            nodes.add(node);
            totalPending++;
            return node;
        }
        throw new BackendUnavailableException(serviceName, "No available backend connection. serviceName=" + serviceName);
    }

    private synchronized void rollback(Node node) {
        if (nodeMap.get(node.handler) != node) {
            return;
        }
        nodes.remove(node);
        if (node.pending > 0) {
            node.pending--;
            node.handler.decreasePending();
            totalPending--;
        }
        if (!node.handler.isAlive()) {
            nodeMap.remove(node.handler);
            totalPending -= node.pending;
            node.pending = 0;
            return;
        }
        node.order = nextOrder++;
        nodes.add(node);
    }

    public synchronized void complete(BackendIOHandler handler) {
        Node node = nodeMap.get(handler);
        if (node == null) {
            return;
        }
        nodes.remove(node);
        if (node.pending > 0) {
            node.pending--;
            handler.decreasePending();
            totalPending--;
        }
        if (!handler.isAlive()) {
            nodeMap.remove(handler);
            totalPending -= node.pending;
            node.pending = 0;
            return;
        }
        node.order = nextOrder++;
        nodes.add(node);
    }

    public synchronized void removeClosed(BackendIOHandler handler) {
        Node node = nodeMap.get(handler);
        if (node == null) {
            return;
        }
        removeNode(node);
    }

    public void maintain() {
        List<BackendIOHandler> handlers;
        synchronized (this) {
            if (closed) {
                return;
            }
            handlers = new ArrayList<>(nodeMap.keySet());
        }
        long now = System.currentTimeMillis();
        for (BackendIOHandler handler : handlers) {
            handler.checkTimeout(now);
        }
        BackendIOHandler removable;
        synchronized (this) {
            removable = shrinkIfNeeded(System.currentTimeMillis());
        }
        if (removable != null) {
            removable.closeByPool();
        }
    }

    public boolean available() {
        return !closed;
    }
    public int totalLoad() {
        if (closed) {
            return Integer.MAX_VALUE;
        }
        return totalPending;
    }

    public boolean sameEndpoint(EndpointSnapshot other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(endpoint.getHost(), other.getHost())
                && endpoint.getPort() == other.getPort()
                && Objects.equals(endpoint.getProtocol(), other.getProtocol())
                && Objects.equals(endpoint.getHealthPath(), other.getHealthPath());
    }

    public void close() {
        List<BackendIOHandler> handlers;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            handlers = new ArrayList<>(nodeMap.keySet());
            nodeMap.clear();
            nodes.clear();
            totalPending = 0;
        }
        for (BackendIOHandler handler : handlers) {
            handler.closeByPool();
        }
    }

    private void growIfNeeded() {
        if (nodes.size() >= maxConnection) {
            return;
        }
        if (nodes.isEmpty()) {
            addConnection();
            return;
        }
        if (nodes.first().pending >= growThreshold) {
            addConnection();
        }
    }

    private BackendIOHandler shrinkIfNeeded(long now) {
        if (nodes.size() <= minConnection) {
            return null;
        }
        Node first = nodes.pollFirst();
        if (first == null) {
            return null;
        }
        Node second = nodes.isEmpty() ? null : nodes.first();
        if (second == null
                || first.pending > shrinkThreshold
                || second.pending > shrinkThreshold
                || !first.handler.removable(now, idleTimeoutMillis)) {
            nodes.add(first);
            return null;
        }
        nodeMap.remove(first.handler);
        totalPending -= first.pending;
        return first.handler;
    }

    private void addConnection() {
        if (closed || nodes.size() >= maxConnection) {
            return;
        }
        BackendIOHandler handler = connectionFactory.create(this);
        handler.setConnectionPool(this);
        Node node = new Node(handler, nextOrder++);
        nodeMap.put(handler, node);
        nodes.add(node);
    }

    private void removeNode(Node node) {
        nodeMap.remove(node.handler);
        nodes.remove(node);
        totalPending -= node.pending;
        node.pending = 0;
    }

    private static final class Node {
        private final BackendIOHandler handler;
        private int pending;
        private long order;

        private Node(BackendIOHandler handler, long order) {
            this.handler = handler;
            this.order = order;
        }
    }
}