package io.jacksoon.common.pipeline.context;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class HttpResponse {
    private int statusCode = 200;
    private String reasonPhrase = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private Object body;
    private ByteBuffer writeBuffer;
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}
