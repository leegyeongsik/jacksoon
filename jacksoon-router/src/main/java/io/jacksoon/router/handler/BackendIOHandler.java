package io.jacksoon.router.handler;
import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.common.util.ResponseCheckResult;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BackendIOHandler extends NioConnectionHandler {
    private final CommonBlockingQueue<ProxyContext> requestQueue;
    private final CommonBlockingQueue<ProxyContext> responseQueue;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final HttpResponseCheck responseCheck;

    private ProxyContext currentWriteContext;

    public BackendIOHandler(Selector selector, SocketChannel socketChannel, CommonBlockingQueue<ProxyContext> requestQueue, CommonBlockingQueue<ProxyContext> responseQueue, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck) {
        super(selector, socketChannel, SelectionKey.OP_CONNECT);
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
    }

    @Override
    public void handle() {
        try {
            if (!selectionKey.isValid()) {
                close();
                return;
            }

            if (selectionKey.isConnectable()) {
                connect();
            }

            if (selectionKey.isReadable()) {
                read();
            }

            if (selectionKey.isWritable()) {
                write();
            }
        } catch (IOException | InterruptedException e) {
            close();
        }

    }

    public void read() throws InterruptedException {
        ProxyContext proxyContext = responseQueue.peek();
        if (proxyContext == null) {
            return;
        }
        ByteBuffer readBuffer = proxyContext.readBuffer;
        readBuffer.clear();
        try {
            int read = socketChannel.read(readBuffer);
            if (read == -1) {
                ResponseCheckResult result = responseCheck.eof(proxyContext.responseBuffer);

                if (!result.complete()) {
                    close();
                    return;
                }

                completeBackendResponse(proxyContext, result, true);
                return;
            }
            if (read == 0) {
                return;
            }
            readBuffer.flip();
            ResponseCheckResult result = responseCheck.check(readBuffer, proxyContext.responseBuffer);
            if (!result.complete()) {
                return;
            }
            completeBackendResponse(proxyContext, result, false);

        } catch (IOException e) {
            close();
        }
    }

    private void connect() throws IOException {
        if (socketChannel.finishConnect()) {
            if (!requestQueue.isEmpty()) {
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void write() throws IOException, InterruptedException {
        if (currentWriteContext == null) {
            if (!responseQueue.isEmpty()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                return;
            }

            currentWriteContext = requestQueue.poll();
            if (currentWriteContext == null) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                return;
            }
        }
        int written = socketChannel.write(currentWriteContext.requestBuffer);
        if (written == 0) {
            return;
        }
        if (currentWriteContext.requestBuffer.hasRemaining()) {
            return;
        }
        responseQueue.put(currentWriteContext);
        currentWriteContext = null;
        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    public void send(ProxyContext context) {
        requestQueue.put(context);

        if (!selectionKey.isValid()) {
            close();
            return;
        }

        if (!socketChannel.isConnected()) {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
            selector.wakeup();
            return;
        }

        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    private void completeBackendResponse(ProxyContext proxyContext, ResponseCheckResult result, boolean backendClosed) throws InterruptedException {
        responseQueue.poll();
        ByteBuffer responseBuffer = proxyContext.responseBuffer;
        responseBuffer.flip();
        responseBuffer.limit(result.responseLength());

        RouterPipelineContext context =
                new RouterPipelineContext(
                        socketChannel,
                        "backend-response",
                        responseBuffer,
                        result.responseLength(),
                        proxyContext.bufferContext,
                        proxyContext.clientKey
                );
        routerPipelineQueue.put(context);
        if (backendClosed) {
            close();
            return;
        }

        if (!requestQueue.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

}
