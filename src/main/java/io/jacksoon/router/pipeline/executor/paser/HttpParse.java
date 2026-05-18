package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.context.RouterRequest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HttpParse implements Parse {
    @Override
    public void parse(PipelineContext context) {
        RouterRequest routerRequest = context.getRequest();

        int headerLength = context.getByteBufferIndex();

        byte[] bytes = new byte[headerLength];

        ByteBuffer buffer = context.getByteBuffer();
        buffer.position(0);
        buffer.get(bytes);

        String header = new String(bytes, StandardCharsets.UTF_8);
        String[] lines = header.split("\r\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("No request line");
        }

        String[] requestLine = lines[0].trim().split(" ");

        if (requestLine.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid request line: [" + lines[0] + "]"
            );
        }

        routerRequest.setMethod(requestLine[0]);
        routerRequest.setPath(requestLine[1]);
        routerRequest.setVersion(requestLine[2]);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            if (line.isEmpty()) {
                break;
            }

            String[] parts = line.split(":", 2);

            String key = parts[0].trim();
            String value = parts[1].trim();

            routerRequest.getHeaders().put(key, value);
        }
        int bodyLength = buffer.limit() - headerLength;

        context.getByteBuffer().position(headerLength);
        context.getByteBuffer().compact();

        context.getByteBuffer().position(0);
        context.getByteBuffer().limit(bodyLength);

        context.setByteBufferIndex(bodyLength) ;
        // 값을 채움
        // 그리고 그니까 순서가 paser -> router면
        // paser 찍고 리턴되고
        // 라우터찍고 리턴되고 이러니까 paser에서 값만 채우면됨
        // 버퍼에서 header는 쉽게 뽑을수 있는데
        // 바디는 안넘겨도됨
        // getter setter 쓸거냐
        // router에서 body를 굳이 뽑아야하냐 안뽑아야하냐 그냥 넘기면 안되나
        // 그리고 만약에 body값이 필요하다면 필터로 빼라 저러면 어차피 재포장하긴해야됨 재포장은 메서드제공해서 까고 메서드로 다시 채우고 이런식으로
    }
}
