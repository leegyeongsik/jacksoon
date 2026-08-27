package io.jacksoon.router.connection.client;

import io.jacksoon.common.handler.ClientConnectionLifecycle;

import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnectionManager implements ClientConnectionLifecycle {
    private final ConcurrentHashMap<SelectionKey, ClientState> clientMap = new ConcurrentHashMap<>();
    private final Set<SelectionKey> coldClients = ConcurrentHashMap.newKeySet();
    private final Set<SelectionKey> warmClients = ConcurrentHashMap.newKeySet();
    private final Set<SelectionKey> hotClients = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<SelectionKey> closeQueue = new LinkedBlockingQueue<>();
    private final ClientConnectionPolicy connectionPolicy;

    public ClientConnectionManager(ClientConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
    }

    @Override
    public void connected(SelectionKey selectionKey, Runnable closeAction) {
        long now = System.currentTimeMillis();
        ClientState state = new ClientState(closeAction, ClientConnectionTier.COLD, now, now + connectionPolicy.coldCheckIntervalMillis());
        clientMap.put(selectionKey, state);
        coldClients.add(selectionKey);
    }

    @Override
    public void readActivity(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }
        synchronized (state) {
            if (state.closing) {
                return;
            }
            state.lastActivityAt = System.currentTimeMillis();
        }
    }

    @Override
    public boolean requestSubmitted(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return false;
        }
        synchronized (state) {
            if (state.closing) {
                return false;
            }
            state.requestCountSinceEvaluation++;
            state.inFlightRequestCount++;
            state.lastActivityAt = System.currentTimeMillis();
            return true;
        }
    }

    @Override
    public void requestFailed(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }
        synchronized (state) {
            if (state.inFlightRequestCount > 0) {
                state.inFlightRequestCount--;
            }
        }
    }

    @Override
    public void responseCompleted(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }
        synchronized (state) {
            if (state.inFlightRequestCount > 0) {
                state.inFlightRequestCount--;
            }
            state.lastActivityAt = System.currentTimeMillis();
        }
    }

    @Override
    public void closed(SelectionKey selectionKey) {
        clientMap.remove(selectionKey);
        coldClients.remove(selectionKey);
        warmClients.remove(selectionKey);
        hotClients.remove(selectionKey);
    }

    public void inspect(ClientConnectionTier tier, long now) {
        if (tier == ClientConnectionTier.CLOSE) {
            throw new IllegalArgumentException("CLOSE tier is handled by ClientConnectionCloseWorker");
        }
        Set<SelectionKey> tierClients = clients(tier);
        for (SelectionKey selectionKey : tierClients) {
            ClientState state = clientMap.get(selectionKey);
            if (state == null) {
                tierClients.remove(selectionKey);
                continue;
            }
            ClientConnectionTier nextTier;
            synchronized (state) {
                if (state.closing) {
                    continue;
                }
                if (state.tier != tier) {
                    continue;
                }
                if (now < state.nextEvaluationAt) {
                    continue;
                }
                long elapsedMillis = Math.max(1L, now - state.lastEvaluatedAt);
                double requestPerSecond = state.requestCountSinceEvaluation * 1000.0 / elapsedMillis;
                long idleMillis = now - state.lastActivityAt;
                state.requestCountSinceEvaluation = 0;
                state.lastEvaluatedAt = now;
                if (tier == ClientConnectionTier.COLD && idleMillis >= connectionPolicy.idleTimeoutMillis() && state.inFlightRequestCount == 0) {
                    nextTier = ClientConnectionTier.CLOSE;
                    state.tier = ClientConnectionTier.CLOSE;
                } else {
                    nextTier = nextTier(tier, requestPerSecond);
                    state.tier = nextTier;
                    state.nextEvaluationAt = now + connectionPolicy.checkIntervalMillis(nextTier);
                }
            }
            if (nextTier == ClientConnectionTier.CLOSE) {
                moveToClose(selectionKey, state, tier);
                continue;
            }
            if (nextTier != tier) {
                move(selectionKey, state, tier, nextTier);
            }
        }
    }

    public void closeNext() throws InterruptedException {
        SelectionKey selectionKey = closeQueue.take();
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }
        Runnable closeAction = null;
        boolean returnToCold = false;
        long now = System.currentTimeMillis();
        synchronized (state) {
            if (state.tier != ClientConnectionTier.CLOSE) {
                return;
            }
            if (state.closing) {
                return;
            }
            long idleMillis = now - state.lastActivityAt;
            if (idleMillis < connectionPolicy.idleTimeoutMillis() || state.inFlightRequestCount > 0) {
                state.tier = ClientConnectionTier.COLD;
                state.requestCountSinceEvaluation = 0;
                state.lastEvaluatedAt = now;
                state.nextEvaluationAt = now + connectionPolicy.coldCheckIntervalMillis();
                returnToCold = true;
            } else {
                state.closing = true;
                closeAction = state.closeAction;
            }
        }

        if (returnToCold) {
            if (clientMap.get(selectionKey) == state) {
                coldClients.add(selectionKey);
            }
            return;
        }
        if (closeAction != null) {
            closeAction.run();
        }
    }

    private ClientConnectionTier nextTier(ClientConnectionTier tier, double requestPerSecond) {
        switch (tier) {
            case COLD:
                if (requestPerSecond >= connectionPolicy.coldToWarmMinRequestPerSecond()) {
                    return ClientConnectionTier.WARM;
                }
                return ClientConnectionTier.COLD;
            case WARM:
                if (requestPerSecond >= connectionPolicy.warmToHotMinRequestPerSecond()) {
                    return ClientConnectionTier.HOT;
                }
                if (requestPerSecond <= connectionPolicy.warmToColdMaxRequestPerSecond()) {
                    return ClientConnectionTier.COLD;
                }
                return ClientConnectionTier.WARM;
            case HOT:
                if (requestPerSecond <= connectionPolicy.hotToWarmMaxRequestPerSecond()) {
                    return ClientConnectionTier.WARM;
                }
                return ClientConnectionTier.HOT;
            case CLOSE:
                throw new IllegalStateException("CLOSE tier does not transition by request rate");
            default:
                throw new IllegalStateException("Unknown tier: " + tier);
        }
    }

    private void move(SelectionKey selectionKey, ClientState state, ClientConnectionTier from, ClientConnectionTier to) {
        clients(from).remove(selectionKey);
        if (clientMap.get(selectionKey) == state) {
            clients(to).add(selectionKey);
        }
    }

    private void moveToClose(SelectionKey selectionKey, ClientState state, ClientConnectionTier from) {
        clients(from).remove(selectionKey);
        if (clientMap.get(selectionKey) == state) {
            closeQueue.offer(selectionKey);
        }
    }

    private Set<SelectionKey> clients(ClientConnectionTier tier) {
        return switch (tier) {
            case COLD -> coldClients;
            case WARM -> warmClients;
            case HOT -> hotClients;
            case CLOSE -> throw new IllegalArgumentException("CLOSE tier uses closeQueue");
        };
    }

    private static final class ClientState {
        private final Runnable closeAction;
        private ClientConnectionTier tier;
        private int requestCountSinceEvaluation;
        private int inFlightRequestCount;
        private long lastActivityAt;
        private long lastEvaluatedAt;
        private long nextEvaluationAt;
        private boolean closing;

        private ClientState(Runnable closeAction, ClientConnectionTier tier, long now, long nextEvaluationAt) {
            this.closeAction = closeAction;
            this.tier = tier;
            this.lastActivityAt = now;
            this.lastEvaluatedAt = now;
            this.nextEvaluationAt = nextEvaluationAt;
        }
    }
}