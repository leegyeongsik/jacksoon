package io.jacksoon.router.help;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
public class BufferContext {
    private ByteBuffer requestBuffer = ByteBuffer.allocate(8 * 1024);
    private ByteBuffer responseBuffer = ByteBuffer.allocate(8*1024);
}