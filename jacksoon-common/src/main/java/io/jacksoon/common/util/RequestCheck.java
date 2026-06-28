package io.jacksoon.common.util;


import java.nio.ByteBuffer;

public interface RequestCheck {
    RequestCheckResult check(ByteBuffer inputByteBuffer , ByteBuffer accumulationByteBuffer);
}
