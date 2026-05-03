package io.jacksoon.router.pipeline;

import java.util.HashMap;
import java.util.Map;

public class StepRegistry {
    Map<String,Object[]> current = new HashMap<>();
    StepRegistry(){
        // read -> event
        // read일때 event에 던지고
        // 얘 싱글톤이니까
        // 객체 하나 만들어서 list로 줄테니까 read면 다음꺼 넘기고
        // executor 다 읽어와서 그걸로 map을 초기화할거임 자기 this 넘겨주고 ㅇㅇ 이전단계 주면되겠다 key는 이전단계 , object는 current step , current , this
        // 그러면 이거를 어케해줄거냐 근데 이렇게했을때 만약에 중간에 바뀌면 그거 생각해야됨
        // 그러면 일단 맵을 두개만들어서 key value Object[]  0 = 로 가져가게끔하고

        // 그러면 object에 두개 넣어놓고 현재 익스큐터 다음 next 스트링 그리고 다음 넘겨줄때 next로 찾아서 넘겨줌]

        // 그러면 초기화를 일단 executor들로 키를 가져오고 current 넣어놓고 연결해놓은거 그 다음꺼 넣어놓음 가령
        // read->paser라면
        // 맨 마지막부터 만들고 object에 넣고 그런식으로 재귀적으로 a->d까지 만들고 d만든거 c에넣고 c만든거 b에 넣고 b만든거 a에넣으면 a는 current가지고있고 그 다음 b가지고있는식으로
        // read만들고 paser만들고

        // 차례대로 만들고 config에 넣어야됨 가령 a b c 라고 한다면 익스큐터로 이벤트랑 this 다 모아서 일단 걔로 스텝을 만들고 다음꺼만들고 리턴되면 걔step이랑 this랑 이벤트로 put함
        // a스텝 일단 만들고 b 스텝 만들고 c스텝만들고 b스텝에 c넣고 a스텝에 b넣고
        // put 해주고
        // 그러면 연결까지는 일단 시켜놓자 이렇게하면 객체를 만들긴하는데 흠;
        // 차라리 이름이랑 레지스트리 주고 각 스텝마다 호출하게하는게
        // 그리고 맨 처음에 만들어진 read write 스텝을 파이프라인 첫단계에 넣어줌 그러면 처음에 read가 호출되면 처음에 read를 실행하고 다음 뎁스로 객체를 바꿔줌 그래서 그걸로 또 그거실행

        // 그냥 name으로 현재 호출하고 다음꺼 바꿔주자
    }

    public PipeLineExecutor getPipeLineExecutor(String event) {
        return (PipeLineExecutor) current.get(event)[0];
    }
    public String getPipelineStep(String event) {
        return (String) current.get(event)[1];
    }
    public void put(String event , PipeLineExecutor pipeLineExecutor, String nextEvent){
        current.put(event,new Object[]{pipeLineExecutor,nextEvent});
    }
}
