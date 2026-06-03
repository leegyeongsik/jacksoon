package io.jacksoon.router.init.registration;

import lombok.Getter;

import java.lang.reflect.Type;
import java.util.List;

@Getter
public class TypeMetadata {

    private final Type sourceType;

    private final Class<?> rawType;

    private final List<TypeMetadata> actualTypeArguments;

    private final boolean wildcard;

    private final List<TypeMetadata> upperBounds;

    public TypeMetadata(
            Type sourceType,
            Class<?> rawType,
            List<TypeMetadata> actualTypeArguments,
            boolean wildcard,
            List<TypeMetadata> upperBounds
    ) {
        this.sourceType = sourceType;
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.wildcard = wildcard;
        this.upperBounds = upperBounds;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TypeMetadata other)) {
            return false;
        }

        if (!rawType.equals(other.rawType)) {
            return false;
        }

        if (wildcard != other.wildcard) {
            return false;
        }

        if (!actualTypeArguments.equals(other.actualTypeArguments)) {
            return false;
        }

        return upperBounds.equals(other.upperBounds);
    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + actualTypeArguments.hashCode();
        result = 31 * result + upperBounds.hashCode();
        result = 31 * result + Boolean.hashCode(wildcard);
        return result;
    }
}
    // 제네릭 정보
    // 리졸버구현해 타게하자

    // 그래프를 어떻게 구성할거냐
    // 타입에서 찾아서 주입시킬거잖아 그치?
    // 저럴거면 그냥 노드를 타입메타데이터가 가져가는게 낫지않나?
    // 주입시킬때 타입메타데이터를 볼거고
    // 그랬을때 여러 정보를 넣는다
    // 어떻게 탐색할거임

    //가령 abdcasc generic 뭔가 이런애가 있다고 해보자
    // 이걸로 타입메타데이터 찾아서
    // 탐색들어가서
    // 만족하는애들 잡아주셈

    // abdcasc generic 이거매칭은
    // 내가 equal 오버라이딩 해서 구현하면 될테고 일단 파라미터 정보로 가져오던가 아니면 그 정보로 맵핑을 해놓던가 객체를
    // 이렇게하면 node 필요없을듯
