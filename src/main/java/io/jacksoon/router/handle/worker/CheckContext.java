package io.jacksoon.router.handle.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Getter
@Setter
@AllArgsConstructor
public class CheckContext {
    ByteBuffer requestBuffer;
    ByteBuffer readBuffer;
    SocketChannel socketChannel;
    SelectionKey selectionKey;
}
