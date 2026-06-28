package io.jacksoon.registry.pipeline.dispatcher;

public class RegistryDispatcher {

    // 여기서 request정보로 찾아줄거임 다음뎁스 얘는 그럼 map으로 들고있어야할듯
    // map으로 가령 request/ 이거면 snapshot주고 이런식으로 다 등록해놓고 config에서

    //컨피그에서 /a -> requestRegister , /b -> registrySnapshotQuery 이런식으로 등록
    // pipeline 인터페이스화해서 각 디렉토리에서 맞는거 구현하게끔 하자 즉 execute부분 구현
    // 레지스트리는 PipelineExecutor 객체 찾아서 context에 넣어주고 execute실행하는 식으로

}
