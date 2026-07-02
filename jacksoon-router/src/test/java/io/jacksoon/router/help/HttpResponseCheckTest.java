//package io.jacksoon.router.help;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//
//class HttpResponseCheckTest {
//
//    private final HttpResponseCheck responseCheck = new HttpResponseCheck();
//
//    @Test
//    void returnsCompleteForResponseWithContentLength() {
//        String response =
//                "HTTP/1.1 200 OK\r\n" +
//                        "Content-Length: 5\r\n" +
//                        "\r\n" +
//                        "hello";
//        ByteBuffer input = buffer(response);
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ResponseCheckResult result = responseCheck.check(input, accumulation);
//
//        Assertions.assertTrue(result.complete());
//        Assertions.assertEquals(response.length(), result.responseLength());
//        Assertions.assertEquals(headerLength(response), result.headerLength());
//        Assertions.assertFalse(result.closeByEof());
//    }
//
//    @Test
//    void returnsIncompleteWhenBodyIsNotFullyReceived() {
//        ByteBuffer input = buffer(
//                "HTTP/1.1 200 OK\r\n" +
//                        "Content-Length: 10\r\n" +
//                        "\r\n" +
//                        "hello"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ResponseCheckResult result = responseCheck.check(input, accumulation);
//
//        Assertions.assertFalse(result.complete());
//        Assertions.assertEquals(0, result.responseLength());
//        Assertions.assertEquals(0, result.headerLength());
//    }
//
//    @Test
//    void supportsSplitResponseAcrossMultipleChecks() {
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ResponseCheckResult first = responseCheck.check(
//                buffer("HTTP/1.1 200 OK\r\nContent-Length: 5\r\n\r\nhe"),
//                accumulation
//        );
//
//        Assertions.assertFalse(first.complete());
//
//        ResponseCheckResult second = responseCheck.check(
//                buffer("llo"),
//                accumulation
//        );
//
//        Assertions.assertTrue(second.complete());
//        Assertions.assertEquals(
//                "HTTP/1.1 200 OK\r\nContent-Length: 5\r\n\r\nhello".length(),
//                second.responseLength()
//        );
//    }
//
//    @Test
//    void acceptsCaseInsensitiveContentLengthHeader() {
//        ByteBuffer input = buffer(
//                "HTTP/1.1 200 OK\r\n" +
//                        "content-length: 5\r\n" +
//                        "\r\n" +
//                        "hello"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ResponseCheckResult result = responseCheck.check(input, accumulation);
//
//        Assertions.assertTrue(result.complete());
//    }
//
//    @Test
//    void throwsExceptionWhenContentLengthIsInvalid() {
//        ByteBuffer input = buffer(
//                "HTTP/1.1 200 OK\r\n" +
//                        "Content-Length: abc\r\n" +
//                        "\r\n"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> responseCheck.check(input, accumulation)
//        );
//    }
//
//    @Test
//    void throwsExceptionWhenResponseIsChunked() {
//        ByteBuffer input = buffer(
//                "HTTP/1.1 200 OK\r\n" +
//                        "Transfer-Encoding: chunked\r\n" +
//                        "\r\n" +
//                        "5\r\nhello\r\n0\r\n\r\n"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> responseCheck.check(input, accumulation)
//        );
//    }
//
//    @Test
//    void eofCompletesResponseWithoutContentLength() {
//        String response =
//                "HTTP/1.1 200 OK\r\n" +
//                        "Connection: close\r\n" +
//                        "\r\n" +
//                        "hello";
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//        accumulation.put(response.getBytes(StandardCharsets.US_ASCII));
//
//        ResponseCheckResult result = responseCheck.eof(accumulation);
//
//        Assertions.assertTrue(result.complete());
//        Assertions.assertEquals(response.length(), result.responseLength());
//        Assertions.assertEquals(headerLength(response), result.headerLength());
//        Assertions.assertTrue(result.closeByEof());
//    }
//
//    @Test
//    void eofWithEmptyBufferReturnsIncompleteAndClosed() {
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ResponseCheckResult result = responseCheck.eof(accumulation);
//
//        Assertions.assertFalse(result.complete());
//        Assertions.assertEquals(0, result.responseLength());
//        Assertions.assertEquals(0, result.headerLength());
//        Assertions.assertTrue(result.closeByEof());
//    }
//
//    @Test
//    void eofBeforeHeaderCompletedThrowsException() {
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//        accumulation.put("HTTP/1.1 200 OK\r\nContent-Length: 5\r\n".getBytes(StandardCharsets.US_ASCII));
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> responseCheck.eof(accumulation)
//        );
//    }
//
//    @Test
//    void throwsExceptionWhenHeaderIsTooLarge() {
//        StringBuilder builder = new StringBuilder("HTTP/1.1 200 OK\r\n");
//        while (builder.length() <= 17 * 1024) {
//            builder.append("X-Test: value\r\n");
//        }
//
//        ByteBuffer input = buffer(builder.toString());
//        ByteBuffer accumulation = ByteBuffer.allocate(32 * 1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> responseCheck.check(input, accumulation)
//        );
//    }
//
//    private ByteBuffer buffer(String text) {
//        return ByteBuffer.wrap(text.getBytes(StandardCharsets.US_ASCII));
//    }
//
//    private int headerLength(String httpMessage) {
//        return httpMessage.indexOf("\r\n\r\n") + 4;
//    }
//}
