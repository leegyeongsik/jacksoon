package io.jacksoon.router.help;
import java.nio.ByteBuffer;

public class ConnectionContext {

    private ByteBuffer requestBuffer = ByteBuffer.allocate(8 * 1024);

    public ByteBuffer getRequestBuffer() {
        return requestBuffer;
    }

    public void setRequestBuffer(ByteBuffer requestBuffer) {
        this.requestBuffer = requestBuffer;
    }
}