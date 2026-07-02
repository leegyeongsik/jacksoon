//package io.jacksoon.router.help;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//
//class HttpRequestCheckTest {
//
//    private final HttpRequestCheck httpRequestCheck = new HttpRequestCheck();
//
//    @Test
//    void returnsCompleteForGetRequestWithoutBody() {
//        ByteBuffer input = buffer(
//                "GET / HTTP/1.1\r\n" +
//                        "Host: localhost\r\n" +
//                        "\r\n"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        RequestCheckResult result =
//                httpRequestCheck.check(input, accumulation);
//
//        Assertions.assertTrue(result.complete());
//        Assertions.assertEquals(input.limit(), result.requestLength());
//        Assertions.assertEquals(input.limit(), result.headerLength());
//    }
//
//    @Test
//    void returnsIncompleteWhenHeaderIsNotFinished() {
//        ByteBuffer input = buffer(
//                "GET / HTTP/1.1\r\n" +
//                        "Host: localhost\r\n"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        RequestCheckResult result =
//                httpRequestCheck.check(input, accumulation);
//
//        Assertions.assertFalse(result.complete());
//        Assertions.assertEquals(0, result.requestLength());
//        Assertions.assertEquals(0, result.headerLength());
//    }
//
//    @Test
//    void returnsCompleteForPostRequestWithBody() {
//        ByteBuffer input = buffer(
//                "POST / HTTP/1.1\r\n" +
//                        "Host: localhost\r\n" +
//                        "Content-Length: 5\r\n" +
//                        "\r\n" +
//                        "hello"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        RequestCheckResult result =
//                httpRequestCheck.check(input, accumulation);
//
//        int expectedLength = input.limit();
//        int expectedHeaderLength =
//                ("POST / HTTP/1.1\r\n" +
//                        "Host: localhost\r\n" +
//                        "Content-Length: 5\r\n" +
//                        "\r\n")
//                        .getBytes(StandardCharsets.US_ASCII)
//                        .length;
//
//        Assertions.assertTrue(result.complete());
//        Assertions.assertEquals(expectedLength, result.requestLength());
//        Assertions.assertEquals(expectedHeaderLength, result.headerLength());
//    }
//
//    @Test
//    void returnsIncompleteWhenBodyIsNotFullyReceived() {
//        ByteBuffer input = buffer(
//                "POST / HTTP/1.1\r\n" +
//                        "Host: localhost\r\n" +
//                        "Content-Length: 10\r\n" +
//                        "\r\n" +
//                        "hello"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        RequestCheckResult result =
//                httpRequestCheck.check(input, accumulation);
//
//        Assertions.assertFalse(result.complete());
//        Assertions.assertEquals(0, result.requestLength());
//        Assertions.assertEquals(0, result.headerLength());
//    }
//
//    @Test
//    void acceptsCaseInsensitiveContentLengthHeader() {
//        ByteBuffer input = buffer(
//                "POST / HTTP/1.1\r\n" +
//                        "content-length: 5\r\n" +
//                        "\r\n" +
//                        "hello"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        RequestCheckResult result =
//                httpRequestCheck.check(input, accumulation);
//
//        Assertions.assertTrue(result.complete());
//        Assertions.assertEquals(input.limit(), result.requestLength());
//    }
//
//    @Test
//    void throwsExceptionWhenContentLengthIsInvalid() {
//        ByteBuffer input = buffer(
//                "POST / HTTP/1.1\r\n" +
//                        "Content-Length: abc\r\n" +
//                        "\r\n"
//        );
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> httpRequestCheck.check(input, accumulation)
//        );
//    }
//
//    @Test
//    void throwsExceptionWhenHeaderIsTooLarge() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("GET / HTTP/1.1\r\n");
//
//        while (builder.length() <= 17 * 1024) {
//            builder.append("X-Test: value\r\n");
//        }
//
//        ByteBuffer input = buffer(builder.toString());
//        ByteBuffer accumulation = ByteBuffer.allocate(32 * 1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> httpRequestCheck.check(input, accumulation)
//        );
//    }
//
//    @Test
//    void throwsExceptionWhenRequestIsTooLarge() {
//        ByteBuffer input =
//                ByteBuffer.allocate(11 * 1024 * 1024);
//        input.put(new byte[input.capacity()]);
//        input.flip();
//
//        ByteBuffer accumulation =
//                ByteBuffer.allocate(11 * 1024 * 1024);
//
//        Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> httpRequestCheck.check(input, accumulation)
//        );
//    }
//
//    @Test
//    void supportsSplitRequestAcrossMultipleChecks() {
//        ByteBuffer accumulation = ByteBuffer.allocate(1024);
//
//        ByteBuffer first = buffer(
//                "POST / HTTP/1.1\r\n" +
//                        "Content-Length: 5\r\n" +
//                        "\r\n" +
//                        "he"
//        );
//
//        RequestCheckResult firstResult =
//                httpRequestCheck.check(first, accumulation);
//
//        Assertions.assertFalse(firstResult.complete());
//
//        ByteBuffer second = buffer("llo");
//
//        RequestCheckResult secondResult =
//                httpRequestCheck.check(second, accumulation);
//
//        Assertions.assertTrue(secondResult.complete());
//
//        int expectedLength =
//                ("POST / HTTP/1.1\r\n" +
//                        "Content-Length: 5\r\n" +
//                        "\r\n" +
//                        "hello")
//                        .getBytes(StandardCharsets.US_ASCII)
//                        .length;
//
//        Assertions.assertEquals(
//                expectedLength,
//                secondResult.requestLength()
//        );
//    }
//
//    private ByteBuffer buffer(String text) {
//        return ByteBuffer.wrap(
//                text.getBytes(StandardCharsets.US_ASCII)
//        );
//    }
//}