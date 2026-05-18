package io.jacksoon.router.seletor;

import io.jacksoon.router.handle.AcceptHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ReactorTest {
    Selector selector;
    ServerSocketChannel serverSocketChannel;
    AcceptHandler acceptHandler;
    Reactor reactor;
    @BeforeEach
    void init() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        acceptHandler = mock(AcceptHandler.class);
        reactor =
                new Reactor(
                        selector,
                        serverSocketChannel,
                        1012,
                        acceptHandler
                );
    }
    @AfterEach
    void cleanup() throws IOException {
        serverSocketChannel.close();
        selector.close();
    }
    @Test
    void constructor() throws IOException {
        assertNotNull(reactor);

        assertTrue(serverSocketChannel.isOpen());

        assertFalse(serverSocketChannel.isBlocking());

        InetSocketAddress address = (InetSocketAddress) serverSocketChannel.getLocalAddress();
        assertEquals(1012, address.getPort());

        assertEquals(1, selector.keys().size());

        Object attachment = serverSocketChannel.keyFor(selector).attachment();
        assertSame(acceptHandler, attachment);

        serverSocketChannel.close();
        selector.close();
    }


    @Test
    void successProcessOnce() throws IOException {
        Selector selector = mock(Selector.class);
        ServerSocketChannel serverSocketChannel = mock(ServerSocketChannel.class);
        AcceptHandler acceptHandler = mock(AcceptHandler.class);
        SelectionKey key = mock(SelectionKey.class);

        Set<SelectionKey> selectedKeys = new HashSet<>();
        selectedKeys.add(key);

        when(serverSocketChannel.socket()).thenReturn(mock(ServerSocket.class));
        when(serverSocketChannel.register(any(), anyInt()))
                .thenReturn(mock(SelectionKey.class));

        when(selector.selectedKeys()).thenReturn(selectedKeys);
        when(key.attachment()).thenReturn(acceptHandler);

        Reactor reactor =
                new Reactor(selector, serverSocketChannel, 1012, acceptHandler);

        reactor.processOnce();

        verify(selector).select();
        verify(acceptHandler).handle();
        assertTrue(selectedKeys.isEmpty());
    }
    @Test
    void successDispatch() throws IOException {
        SelectionKey key = mock(SelectionKey.class);
        when(key.attachment()).thenReturn(acceptHandler);

        reactor.dispatch(key);
        verify(acceptHandler).handle();
    }

}