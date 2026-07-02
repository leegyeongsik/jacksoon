package io.jacksoon.common.util;

import java.nio.ByteBuffer;

public interface ResponseCheck {
    ResponseCheckResult check(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer);

    ResponseCheckResult eof(ByteBuffer accumulationByteBuffer);
}