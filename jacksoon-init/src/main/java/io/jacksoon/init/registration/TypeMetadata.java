package io.jacksoon.init.registration;

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