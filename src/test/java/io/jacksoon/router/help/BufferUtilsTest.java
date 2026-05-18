package io.jacksoon.router.help;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static io.jacksoon.router.help.BufferUtils.ensureCapacity;

class BufferUtilsTest {
    @Test
    void returnsSameBufferWhenRemainingIsEnough() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        ByteBuffer result = BufferUtils.ensureCapacity(buffer, 512);

        Assertions.assertSame(buffer, result);
        Assertions.assertEquals(1024, result.capacity());
        Assertions.assertEquals(0, result.position());
    }

    @Test
    void expandsBufferWhenRemainingIsNotEnough() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        ByteBuffer result = BufferUtils.ensureCapacity(buffer, 2028);

        Assertions.assertNotSame(buffer, result);
        Assertions.assertEquals(2048, result.capacity());

        Assertions.assertEquals(0, result.position());
    }

    @Test
    void preservesExistingDataAfterExpansion() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put("abcd".getBytes());

        ByteBuffer result = BufferUtils.ensureCapacity(buffer, 10);

        Assertions.assertEquals(16, result.capacity());

        Assertions.assertEquals(4, result.position());

        result.flip();
        byte[] bytes = new byte[4];
        result.get(bytes);

        Assertions.assertArrayEquals("abcd".getBytes(), bytes);
    }

    @Test
    void keepsPositionAfterExpansion() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("hello".getBytes());

        ByteBuffer result = BufferUtils.ensureCapacity(buffer, 20);

        Assertions.assertEquals(32, result.capacity());
        Assertions.assertEquals(5, result.position());
    }

    @Test
    void throwsExceptionWhenRequestExceedsMaxSize() {
        ByteBuffer buffer = ByteBuffer.allocate(8);

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> BufferUtils.ensureCapacity(buffer, 20 * 1024 * 1024)
        );
    }
}