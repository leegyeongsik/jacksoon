package io.jacksoon.router.config;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

@Init
public class ReactorConfig {
    @Init("clientSelector")
    public Selector clientSelector() throws IOException {
        return Selector.open();
    }

    @Init("backendSelector")
    public Selector backendSelector() throws IOException {
        return Selector.open();
    }

    @Init("clientServerSocket")
    public ServerSocketChannel clientServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Init("clientReactor")
    public Reactor clientReactor(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, @Init("acceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1012));
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("acceptHandler")
    public AcceptHandler acceptHandler(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, RequestPipelineQueue queue, HttpRequestCheck check) {
        return new AcceptHandler(selector, serverSocketChannel, queue, check);
    }

    @Init("backendReactor")
    public Reactor backendReactor(@Init("backendSelector") Selector selector) throws Exception {
        return new Reactor(selector);
        // 여기서 라우터에서 레지스트리 찾아서 리액트큐에 집어넣음
        // 리액트큐에서 커넥션핸들러로 객체 생성함 그리고 엔드포인트소켓채널 연결함
        // write모드로 바꾸고
        // 걔가 데이터 보냄
        // 그리고 read모드로 변경
        // 그 엔드포인트소켓채널에서 read가 오면 처리해서 clientreactor큐에 넣음
        // 해당 소켓의 상태를 write로 바꿈
        // 꺼낼때 해당 버퍼큐에 데이터 넣음
        // write
        // 레지스트리는 그냥 스프링하나 만들어서 A요청받으면 그거 타도록 하나 임시로
    }

}
