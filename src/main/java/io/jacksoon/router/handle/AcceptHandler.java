package io.jacksoon.router.handle;

import io.jacksoon.router.help.ConnectionContext;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler {
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;
    final RequestPipelineQueue requestPipelineQueue;
    final HttpRequestCheck httpRequestCheck;
    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel, RequestPipelineQueue requestPipelineQueue , HttpRequestCheck httpRequestCheck) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.requestPipelineQueue = requestPipelineQueue;
        this.httpRequestCheck = httpRequestCheck;
    }

    @Override
    public void handle() {
        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            // 여기서 accpet할때 등록해놓은 어셉트해주는 채널이 일할수있다고 해서 key로 붙은 handler를 찾음
            //final Selector selector;
            //final ServerSocketChannel serverSocketChannel;
            //final ReadAndWriteGo writeGo;
            // 어셉트 핸들러 만들면서 채널 초기화도 해줬음
            // 그래서 accpet는 논 블로킹으로 대기안함 바로 갖고옴  poll 같은느낌 그래서 큐에 쌓여있는 클라이언트 소켓 가져옴
            // 그래서 채널이 null일수있는 이유가 accpet해서 queue에 쌓여있는게 없으면 null이고 있으면 클라이언트 소켓이 옴
            // 그 소켓을 다시 채널에 등록  new EchoHandler(writeGo,selector, socketChannel);
            if (socketChannel != null) {
                new IOHandler(requestPipelineQueue,selector, socketChannel,httpRequestCheck,new ConnectionContext());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}