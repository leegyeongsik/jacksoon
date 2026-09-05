package io.jacksoon.common.util;

import java.nio.ByteBuffer;

public record ResponseContext(int sequence, ByteBuffer byteBuffer, boolean closeAfterWrite) {
}
