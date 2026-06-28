package io.jacksoon.common.util;

import java.nio.ByteBuffer;

public final class BufferUtils {

    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;

    private BufferUtils() {
    }

    public static ByteBuffer ensureCapacity(ByteBuffer buffer, int requiredBytes) {
        if (buffer.remaining() >= requiredBytes) {
            return buffer;
        }

        int newCapacity = buffer.capacity();

        while ((newCapacity - buffer.position()) < requiredBytes) {
            newCapacity *= 2;

            if (newCapacity > MAX_REQUEST_SIZE) {
                throw new IllegalStateException("Request too large");
            }
        }

        buffer.flip();

        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        newBuffer.put(buffer);

        return newBuffer;
    }
}