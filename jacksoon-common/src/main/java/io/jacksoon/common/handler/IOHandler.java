package io.jacksoon.common.handler;

import io.jacksoon.common.util.BufferUtils;
import io.jacksoon.common.util.RequestCheck;
import io.jacksoon.common.util.RequestCheckResult;
import io.jacksoon.common.util.ResponseContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IOHandler implements Handler {
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0).asReadOnlyBuffer();

    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer readBuffer = ByteBuffer.allocate(8 * 1024);
    ByteBuffer totalBuffer = ByteBuffer.allocate(8 * 1024);
    final RequestCheck requestCheck;
    final RequestSubmitter requestSubmitter;
    final IOStore ioStore;
    final ClientConnectionLifecycle connectionLifecycle;
    private ResponseContext currentWriteResponse;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicInteger nextRequestSequence = new AtomicInteger(1);

    public IOHandler(Selector selector, SocketChannel socketChannel, RequestCheck requestCheck, RequestSubmitter requestSubmitter, IOStore ioStore, ClientConnectionLifecycle connectionLifecycle) throws IOException {
        this.requestSubmitter = requestSubmitter;
        this.socketChannel = socketChannel;
        this.requestCheck = requestCheck;
        this.connectionLifecycle = connectionLifecycle;
        this.socketChannel.configureBlocking(false);
        selectionKey = this.socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        this.ioStore = ioStore;
        ioStore.initClient(selectionKey);
        connectionLifecycle.connected(selectionKey, this::close);
        selector.wakeup();
    }

    @Override
    public void handle() {
        try {
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isReadable()) {
                boolean alive = read();
                if (!alive) {
                    return;
                }
            }
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isWritable()) {
                write();
            }
        } catch (IOException | CancelledKeyException ex) {
            close();
        }
    }

    boolean read() throws IOException {
        int readCount = socketChannel.read(readBuffer);
        if (readCount == -1) {
            close();
            return false;
        }
        if (readCount == 0) {
            return true;
        }
        connectionLifecycle.readActivity(selectionKey);
        readBuffer.flip();
        totalBuffer = BufferUtils.ensureCapacity(totalBuffer, readBuffer.remaining());
        RequestCheckResult result = requestCheck.check(readBuffer, totalBuffer);
        readBuffer.clear();
        processCompletedRequests(result);
        return !closed.get();
    }

    private void processCompletedRequests(RequestCheckResult firstResult) {
        RequestCheckResult result = firstResult;
        while (result.complete()) {
            ByteBuffer ownedRequest = copyRequest(totalBuffer, result.requestLength());
            removeConsumedBytes(totalBuffer, result.requestLength());
            int sequence = nextRequestSequence.getAndIncrement();
            boolean accepted = connectionLifecycle.requestSubmitted(selectionKey);
            if (!accepted) {
                close();
                return;
            }
            try {
                requestSubmitter.submit(socketChannel, ownedRequest, result.headerLength(), selectionKey, new AtomicInteger(sequence));
            } catch (RuntimeException ex) {
                connectionLifecycle.requestFailed(selectionKey);
                throw ex;
            }
            if (totalBuffer.position() == 0) {
                return;
            }
            result = requestCheck.check(EMPTY_BUFFER.duplicate(), totalBuffer);
        }
    }

    private ByteBuffer copyRequest(ByteBuffer accumulation, int requestLength) {
        ByteBuffer source = accumulation.duplicate();
        source.flip();
        source.limit(requestLength);
        ByteBuffer owned = ByteBuffer.allocate(requestLength);
        owned.put(source);
        owned.flip();
        return owned;
    }

    private void removeConsumedBytes(ByteBuffer accumulation, int consumedBytes) {
        accumulation.flip();
        accumulation.position(consumedBytes);
        accumulation.compact();
    }

    void write() throws IOException {
        while (selectionKey.isValid()) {
            if (currentWriteResponse == null) {
                currentWriteResponse = ioStore.pollReadyOrDisableWrite(selectionKey);
                if (currentWriteResponse == null) {
                    return;
                }
            }
            ByteBuffer byteBuffer = currentWriteResponse.byteBuffer();
            while (byteBuffer.hasRemaining()) {
                int written = socketChannel.write(byteBuffer);
                if (written == 0) {
                    return;
                }
            }
            boolean closeAfterWrite = currentWriteResponse.closeAfterWrite();
            currentWriteResponse = null;
            ioStore.responseCompleted(selectionKey);
            connectionLifecycle.responseCompleted(selectionKey);
            if (closeAfterWrite) {
                close();
                return;
            }
        }
    }

    void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        connectionLifecycle.closed(selectionKey);
        ioStore.removeClient(selectionKey);
        try {
            selectionKey.cancel();
        } catch (Exception ignored) {
        }
        try {
            socketChannel.close();
        } catch (IOException ignored) {
        }
    }
}