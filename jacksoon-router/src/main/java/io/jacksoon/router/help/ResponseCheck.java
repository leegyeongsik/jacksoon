package io.jacksoon.router.help;

import java.nio.ByteBuffer;

public interface ResponseCheck {
    ResponseCheckResult check(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer);

    ResponseCheckResult eof(ByteBuffer accumulationByteBuffer);
}