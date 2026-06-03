package io.jacksoon.router.init.registration;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
@Getter
public class InitNode {
    Class<?> clazz;
    Set<InitNode> parents;
    Set<InitNode> children;
    public InitNode(Class<?> clazz){
        this.clazz = clazz;
        this.parents = new HashSet<>();
        this.children = new HashSet<>();
    }
    public void putParent(InitNode initNode){
        this.parents.add(initNode);
    }
    public void putChildren(InitNode initNode){
        this.children.add(initNode);
    }
}
// 체크를 어케할까
// type에서 할까 그러면 그게 될라나 type으로 하면 완성 됬다는거니까 리즈너블 하긴함 타입에서 갖고와서 initNode 넣어주면됨 그거는 좀 편함
// 아근데 이거 사이클날거같은데 위만 일단 보면 모르겠는데 만약에 a가 b를 상속하고있을떄 a에서 b를 만들고 b는 a를 만들고 이러면 안되는디
// 그러면 한쪽 훑고 다 훑었으면 아래쪽 훑고 그때 완성됬다고 하고 true로 해주고
// 그래프를 만든다고 했을때 일단 리스트로 클래스를 다 넣어놓고
// typemap에서 하나하나 들어가서
// 만들어진 그래프 찾아서 넣는거 이거 나쁘지않을거같은데
// 그러면 리스트에 type들이 있고
// 현재 init이라고 했을때 부모를 찾고 생성될일은 없음

// InitNode리스트 만들어서
// type확인하고 부모 자식 체크해서 넣고 끝임

// 타입에서는 타입 정보까지만 찾고
// 노드에서는 관계까지 찾고
// 즉 현재 타입의 정보까지를 담고 그걸 활용
// 가령 내 타입이 ? 이면 파라미터도 ? 로 보는거고
// 그러면 유추를 해야겠지

// 그러면 저렇게 타고들어가서 리스트업하고 가능한놈 추리는거임
// 인터페이스 -> 클래스 -> 클래스  이렇게해서 리스트업하고 -> 가능한지 안한지
