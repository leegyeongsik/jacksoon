package io.jacksoon.router.pipeline.context;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class RouterRequest {
    String method;
    String path;
    String version;
    Map<String, String> headers;
    RouterRequest(){
        headers =  new HashMap<>();
    }
}
