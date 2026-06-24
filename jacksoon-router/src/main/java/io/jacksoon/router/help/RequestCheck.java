package io.jacksoon.router.help;

import java.nio.ByteBuffer;

public interface RequestCheck {
    RequestCheckResult check(ByteBuffer inputByteBuffer , ByteBuffer accumulationByteBuffer);
}
