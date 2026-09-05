package io.jacksoon.common.util;

import java.nio.ByteBuffer;

public final class BufferUtils {

    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;
    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024;

    private BufferUtils() {
    }

    public static ByteBuffer ensureCapacity(ByteBuffer buffer, int requiredBytes) {
        return ensureCapacity(buffer, requiredBytes, MAX_REQUEST_SIZE, "Request too large");
    }
    public static ByteBuffer ensureResponseCapacity(ByteBuffer buffer, int requiredBytes) {
        return ensureCapacity(buffer, requiredBytes, MAX_RESPONSE_SIZE, "Response too large");
    }
    private static ByteBuffer ensureCapacity(ByteBuffer buffer, int requiredBytes, int maxCapacity, String overflowMessage) {
        if (buffer.remaining() >= requiredBytes) {
            return buffer;
        }
        int newCapacity = buffer.capacity();
        while ((newCapacity - buffer.position()) < requiredBytes) {
            if (newCapacity >= maxCapacity) {
                throw new IllegalStateException(overflowMessage);
            }
            int doubled = newCapacity << 1;
            newCapacity = Math.min(doubled, maxCapacity);
        }
        buffer.flip();
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        newBuffer.put(buffer);
        return newBuffer;
    }
}