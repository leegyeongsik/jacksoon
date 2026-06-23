package io.jacksoon.router.seletor;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.handle.Handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
public class Reactor implements Runnable{
    final Selector selector;
    public Reactor(Selector selector) throws IOException {
//        this.serverSocketChannel = serverSocketChannel;
//         open그냥 클래스만들어서 생성때리고 주입받으셈
//
        this.selector = selector;
// 리액터를 좀 수정해야겠다 클라이언트는 port가 필요한데 백엔드는 port가 필요없음 어쨋든 일반화해서 핸들러만 붙이면되는거니까 소켓열고
//        this.acceptHandler = acceptHandler;
// 연결하는 accept도 스레드하나 만들어서 해야겠네 selector에 이벤트주고 붙이는게 연결스레드 , 소켓연결스레드 셀렉터가 쟤네들한테 이벤트줌
        // 그냥 객체하나 만들어서 그거 주입시킬까
        // 일단 yml쓰는건 후순위 저걸로 일단 할게 port번호 할당시켜서 설정클래스만들어서 주입받아서 클라이언트리액터에 주입시키는거
//        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 그러면 재활용할수있을거같음
        // 그러면 채널이랑 셀렉터랑 바인딩하는거랑 그런것만 해결하고 포트만 해결하면
        // 재활용가능

    }

    public void register(SelectableChannel channel, Handler handler ,int ops) throws IOException {
        channel.configureBlocking(false);
        SelectionKey key = channel.register(selector, ops);
        key.attach(handler);
    }
    @Override
    public void run() {
        try {
            while (true) {
                processOnce();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void processOnce() throws IOException {
        selector.select();
        Set<SelectionKey> selected = selector.selectedKeys();
        for (SelectionKey key : selected) {
            dispatch(key);
        }
        selected.clear();
    }
     void dispatch(SelectionKey selectionKey) {
        Handler handler = (Handler) selectionKey.attachment();
        handler.handle();
    }
}
