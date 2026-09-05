package io.jacksoon.filterManagement.pipeline.parse;

import io.jacksoon.common.filter.FilterUploadRequest;
import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.filterManagement.exception.InvalidFilterRequestException;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.FilterRequestParser;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class FilterParse implements FilterDepth {
    private final FilterRequestParser requestParser;
    public FilterParse(FilterRequestParser requestParser) {
        this.requestParser = requestParser;
    }
    // 이게 레지스트리를 좀더 고도화했으면 분기를 좀더 잘 할수있었을거같은데 그니까 찾을때 string으로 get 해오지말고 레지스트리가 인스턴스를 가지고있는 구조로
    // 그랬을때 디스패처에서 현재 파이프라인이 이거고 path가 이거일때 인스턴스 가령 paser일때 path가 a다 그러면 그걸로 파이프라인 찾아서 실행하고 즉 event로 set해서 path를 덮지않게끔
    // paser -> a , b , c a는 aa파이프로 b는 bb파이프로 c는 cc파이프로 a가 aa로 갔을때 거기서는 이제 aaa파이프로 그런식으로 객체를 넣어놓고 그걸로 실행하게끔 했던게 더 나았을듯

    @Override
    public void dodo(FilterPipelineContext context) {
        HttpRequest request = context.getRequest();
        ByteBuffer buffer = context.getByteBuffer().duplicate();

        parseHeader(buffer, context.getByteBufferIndex(), request);
        parseBody(buffer, context.getByteBufferIndex(), buffer.limit(), request);

        String method = request.getMethod();
        String path = request.getPath();
        if ("GET".equals(method) && "/version".equals(path)) {
            context.setEvent("version-read");
            return;
        }
        if ("GET".equals(method) && "/bundle".equals(path)) {
            context.setEvent("bundle-read");
            return;
        }
        if (("POST".equals(method) || "PUT".equals(method)) && "/filter".equals(path)) {
            FilterUploadRequest uploadRequest = requestParser.parseUpload(context);
            context.setFilterUploadRequest(uploadRequest);
            context.setFilterName(uploadRequest.config().filterName());
            context.setEvent("get-lock");
            return;
        }
        if ("DELETE".equals(method) && "/filter".equals(path)) {
            context.setFilterName(requestParser.parseFilterName(context));
            context.setEvent("get-lock");
            return;
        }
        context.getResponse().setStatusCode(404);
        context.getResponse().setReasonPhrase("Not Found");
        context.getResponse().setWriteBuffer(ByteBuffer.wrap("Not Found".getBytes(StandardCharsets.UTF_8)));
        context.getResponse().addHeader("Content-Type", "text/plain; charset=UTF-8");
        context.setEvent("write");
    }
    private void parseHeader(ByteBuffer buffer, int headerLength, HttpRequest request) {
        byte[] headerBytes = new byte[headerLength];
        buffer.position(0);
        buffer.get(headerBytes);

        String[] lines = new String(headerBytes, StandardCharsets.UTF_8).split("\\r\\n");
        if (lines.length == 0) {
            throw new InvalidFilterRequestException("HTTP request line is missing");
        }

        String[] requestLine = lines[0].trim().split(" ");
        if (requestLine.length < 3) {
            throw new InvalidFilterRequestException("Invalid HTTP request line: " + lines[0]);
        }

        request.setMethod(requestLine[0].toUpperCase());
        request.setPath(requestLine[1]);
        request.setVersion(requestLine[2]);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                request.getHeaders().put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    private void parseBody(ByteBuffer buffer, int headerLength, int requestLength, HttpRequest request) {
        int bodyLength = requestLength - headerLength;
        if (bodyLength <= 0) {
            request.setBody(new byte[0]);
            return;
        }
        byte[] body = new byte[bodyLength];
        buffer.position(headerLength);
        buffer.get(body);
        request.setBody(body);
    }

    @Override
    public String currentEvent() {
        return "parse";
    }
}
