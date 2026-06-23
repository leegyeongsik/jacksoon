package io.jacksoon.router.worker.connection;

import io.jacksoon.router.handle.BackendIOHandler;
import io.jacksoon.router.help.HttpResponseCheck;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.executor.router.ConnectionContexts;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Init
public class ConnectionManager{
    private final RequestPipelineQueue pipelineQueue;
    private final Selector selector;
    private final ConnectionRegistry connectionRegistry;
    private final HttpResponseCheck responseCheck;
    public ConnectionManager(RequestPipelineQueue pipelineQueue, @Init("backendSelector")Selector selector, ConnectionRegistry connectionRegistry, HttpResponseCheck responseCheck) {
        this.pipelineQueue = pipelineQueue;
        this.selector = selector;
        this.connectionRegistry = connectionRegistry;
        this.responseCheck = responseCheck;
    }

    public void create(ConnectionContext connectionContext) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(
                    new InetSocketAddress(
                            connectionContext.getHost(),
                            connectionContext.getPort()
                    )
            );

            RequestBackendQueue requestBackendQueue =  new RequestBackendQueue();
            ResponseBackendQueue  responseBackendQueue = new ResponseBackendQueue();
            BackendIOHandler backendIOHandler =  new BackendIOHandler(selector,socketChannel,responseBackendQueue,requestBackendQueue,pipelineQueue,responseCheck);
            connectionRegistry.put("a", // 등록할때 router/api/user 는 user서버로 뭐 그런식으로 등록해야되고
                    new ConnectionContexts(backendIOHandler.getSelectionKey(),requestBackendQueue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 그러면 엔드포인드도 필요하고 일단 그런데 그걸 어케연결하지
    // 이거 연결하고 풀에 넣어놔야댐
    // 커넥션 객체 생성
    // 소켓을 어떻게 핼거고 // 소켓 어케해서 만들거임 일단 초기화할떄 핸들 생성할거고 거기서 소켓찾는건 오케이 그러면 초기화할때 생성하는 부분인건데
    // handle에서 소켓이 뭔질알고 생성할거고
    // 그 소켓에 지금 연결되있는 요청이 뭔지 그거는 그냥 map으로 해도 될거같긴한데
    // 일단 어떻게 생성할거냐
    // 근데 저거 그냥 커넥션 하나가 잡고있어야할거같음 리턴올때까지 아니면 누가 누구 소켓인지 모를거아님 그거는 http/2 일때 가능할듯 근데 정보 주고 잘하면 될거같은데 후순위
    // 핸들은 그러면 커넥션 객체를 생성하는 역할인거고
    // 커넥션은 커넥션
    // 그냥
    // 근데 굳이? 어떻게 호출할건데 객체 하나 만든다고 치고
    // 그냥 커넥션 스레드만드셈

}
