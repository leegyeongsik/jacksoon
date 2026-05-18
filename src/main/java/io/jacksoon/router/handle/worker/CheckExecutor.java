package io.jacksoon.router.handle.worker;

import io.jacksoon.router.help.BufferUtils;
import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.help.RequestCheckResult;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.step.Step;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class CheckExecutor implements Executor {
    RequestCheck requestCheck;
    RequestPipelineQueue requestPipelineQueue;
    Step step;
    CheckExecutor(RequestCheck requestCheck,RequestPipelineQueue requestPipelineQueue, Step step){
        this.requestCheck = requestCheck;
        this.requestPipelineQueue = requestPipelineQueue;
        this.step =step;
    }
    @Override
    public void executor(CheckContext checkContext) {
        ByteBuffer requestBuffer = checkContext.getRequestBuffer();
        requestBuffer = BufferUtils.ensureCapacity(requestBuffer, checkContext.getReadBuffer().remaining());
        checkContext.setRequestBuffer(requestBuffer);
        RequestCheckResult result = requestCheck.check(checkContext.getReadBuffer(), requestBuffer);
        checkContext.getReadBuffer().clear();
        if (!result.complete()) {
            return;
        }
        requestBuffer.flip();
        requestBuffer.limit(result.requestLength());
        ByteBuffer requestSlice = requestBuffer.slice();
        requestPipelineQueue.put(new PipelineContext(checkContext.socketChannel, step, "READ", requestSlice, result.headerLength()));
        checkContext.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        checkContext.getSelectionKey().selector().wakeup();
    }
}
