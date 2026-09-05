package io.jacksoon.common.pipeline.context;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class HttpRequest {
    String method;
    String path;
    String version;
    Map<String, String> headers;
    HttpRequest(){
        headers =  new HashMap<>();
    }
    byte[] body;
}
