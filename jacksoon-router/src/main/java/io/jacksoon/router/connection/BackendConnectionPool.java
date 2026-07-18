package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;
import io.jacksoon.router.handler.BackendIOHandler;
import io.jacksoon.router.pipeline.context.ProxyContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class BackendConnectionPool {
    private final EndpointSnapshot endpoint;
    private final BackendConnectionFactory connectionFactory;
    private final int minConnection = 1;
    private final int maxConnection = 10;
    private final int growThreshold = 3;
    private final int shrinkThreshold = 0;
    private final long idleTimeoutMillis = 30_000L;
    private final Map<BackendIOHandler, Node> nodeMap = new IdentityHashMap<>();
    private Node head;
    private Node tail;
    private int size;
    private boolean closed;
    public BackendConnectionPool( EndpointSnapshot endpoint, BackendConnectionFactory connectionFactory) {
        this.endpoint = endpoint;
        this.connectionFactory = connectionFactory;
        init();
    }
    private void init() {
        for (int i = 0; i < minConnection; i++) {
            addConnection();
        }
    }
    public EndpointSnapshot endpoint() {
        return endpoint;
    }
    public synchronized void send(ProxyContext context) {
        if (closed) {
            throw new IllegalStateException("Backend connection pool is closed");
        }
        int attempts = 0;
        while (attempts++ < maxConnection) {
            if (head == null) {
                addConnection();
            }
            growIfNeeded();
            Node node = head;
            if (node == null) {
                throw new IllegalStateException("No backend connection");
            }
            detach(node);
            BackendIOHandler handler = node.handler;
            if (!handler.isAlive()) {
                nodeMap.remove(handler);
                size--;
                continue;
            }
            handler.increasePending();
            insertSorted(node);
            boolean accepted = handler.send(context);
            if (accepted) {
                return;
            }
        }
        throw new IllegalStateException("Failed to send request to backend connection");
    }
    public synchronized void complete(BackendIOHandler handler) {
        Node node = nodeMap.get(handler);
        if (node == null) {
            return;
        }
        detach(node);
        handler.decreasePending();
        if (handler.isAlive()) {
            insertSorted(node);
        } else {
            nodeMap.remove(handler);
            size--;
        }
    }
    public synchronized void removeClosed(BackendIOHandler handler) {
        Node node = nodeMap.remove(handler);
        if (node == null) {
            return;
        }
        detach(node);
        size--;
    }
    public synchronized void maintain() {
        if (closed) {
            return;
        }
        shrinkIfNeeded();
    }
    public synchronized boolean available() {
        return !closed;
    }
    public synchronized int load() {
        if (closed) {
            return Integer.MAX_VALUE;
        }
        if (head == null) {
            return 0;
        }
        return head.load();
    }
    public synchronized boolean sameEndpoint(EndpointSnapshot other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(endpoint.getHost(), other.getHost())
                && endpoint.getPort() == other.getPort()
                && Objects.equals(endpoint.getProtocol(), other.getProtocol())
                && Objects.equals(endpoint.getHealthPath(), other.getHealthPath());
    }
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            current.prev = null;
            current.next = null;
            current.handler.closeByPool();
            current = next;
        }
        head = null;
        tail = null;
        nodeMap.clear();
        size = 0;
    }
    private void growIfNeeded() {
        if (size >= maxConnection) {
            return;
        }
        if (head == null) {
            addConnection();
            return;
        }
        if (head.load() >= growThreshold) {
            addConnection();
        }
    }
    private void shrinkIfNeeded() {
        if (size <= minConnection) {
            return;
        }
        if (head == null || head.next == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Node first = head;
        Node second = head.next;
        if (first.load() <= shrinkThreshold && second.load() <= shrinkThreshold && first.handler.removable(now, idleTimeoutMillis)) {
            remove(first.handler);
        }
    }

    private void addConnection() {
        if (closed) {
            return;
        }
        if (size >= maxConnection) {
            return;
        }

        BackendIOHandler handler = connectionFactory.create(this);
        handler.setConnectionPool(this);
        Node node = new Node(handler);
        nodeMap.put(handler, node);
        insertSorted(node);
        size++;
    }
    private void remove(BackendIOHandler handler) {
        Node node = nodeMap.remove(handler);
        if (node == null) {
            return;
        }
        detach(node);
        size--;
        handler.closeByPool();
    }
    private void insertSorted(Node node) {
        node.prev = null;
        node.next = null;
        if (head == null) {
            head = node;
            tail = node;
            return;
        }
        Node current = head;
        while (current != null && current.load() <= node.load()) { // current가 큰놈위치를 찾음
            current = current.next;
        }
        if (current == null) { // null이면 없다는거니까 마지막에 삽입 마지막 next는 node 노드 prev는 마지막
            tail.next = node;
            node.prev = tail;
            tail = node;
            return;
        }
        if (current == head) { // head라는거는 처음인거니까 첫번째로하는데 next는 current , current prev는 node
            node.next = head;
            head.prev = node;
            head = node;
            return;
        }
        Node prev = current.prev;
        prev.next = node;
        node.prev = prev;
        node.next = current;
        current.prev = node;
    }
    private void detach(Node node) {
        Node prev = node.prev;
        Node next = node.next;
        if (prev != null) { // 일단 node의 연결을 끊음 그러면 노드 왼쪽은 노드 오른쪽이랑 연결하고 오른쪽은 왼쪽이랑 연결
            prev.next = next;
        } else {
            head = next;
        }
        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
        node.prev = null;
        node.next = null;
    }
    private static class Node {
        private final BackendIOHandler handler;
        private Node prev;
        private Node next;

        private Node(BackendIOHandler handler) {
            this.handler = handler;
        }
        private int load() {
            return handler.load();
        }
    }
}