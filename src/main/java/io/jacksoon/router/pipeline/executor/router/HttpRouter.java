package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.handle.ProxyContext;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.executor.Depth;
import io.jacksoon.router.seletor.ReactorQueue;
import io.jacksoon.router.worker.connection.ConnectionContext;
import io.jacksoon.router.worker.connection.RequestBackendQueue;

import java.nio.channels.SelectionKey;

@Init
public class HttpRouter implements Depth {
    private final FindRouter findRouter;
    public HttpRouter(FindRouter findRouter) {
        this.findRouter = findRouter; // 파싱하는거 request만든거 어케쓸건지 필터는 어떻게
        // 커넥션 레지스트리에서 가져와서 관리한다고 했을때 파싱해서 /user면 user가져오게끔 그런거 어케할건지
    }

    @Override
    public void dodo(PipelineContext context) {
        ConnectionContexts connectionContext  = findRouter.getConnection(context.getRequest()); // 여기서 queue로 보낼때 request에 있는걸 다시 buffer로 바꿔야댐
        connectionContext.getRequestBackendQueue().put(new ProxyContext(context.getByteBuffer(),context.getResponse().getBufferContext(),context.getResponse().getSelectionKey()));
        SelectionKey key = connectionContext.getSelectionKey();
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        connectionContext.getSelectionKey().selector().wakeup();
    }
    @Override
    public String currentEvent() {
        return "router";
    }
    @Override
    public String nextEvent() {
        return "";
    }
}
