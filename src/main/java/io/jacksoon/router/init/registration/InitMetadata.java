package io.jacksoon.router.init.registration;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public abstract class InitMetadata{
    String name;
    Class<?> clazz;
    TypeMetadata typeMetadata;

    // 가령 a를 찾았다 그러면 initNode 만들고 그러면 좀 많이 생성될거같은데 일단 타입만 넣어주면되긴함 그니까 이게 중복이 생길거같음
    // 저거를 공유하는 객체로 봐야햐나 아니면 a b c마다 봐야하나 이건데 근데 어차피
    // 클래스가있고 그 클래스에서 계속 올라가거나 내려가는식이니까 하나의 타입이 다음걸 봤으면
    // 그래서 공유하는 객체로 봐야할거같은데
    // 얘도 그러면 하나의 타입이 완성됬으면 완성된거 그냥 갖다 쓰면됨
    public InitMetadata(Class<?> clazz,String name){
        this.clazz = clazz;
        this.name =name;
    }
    // 썸띵을 체크하는것도 나쁘지않음 그니까 class에서도 쓰고 method에서 쓰는거를
    public abstract InitInstance createInstance(); // 후보찾아와라면 말이 되는데 아니면 여기서 저길로 가는식으로 하면 될거같긴한데 근데 이거 역으로맵핑이 안되서 좀 삑날거같은데
    // createinstance로 만 해도 될거같음 create안쓰고
}
