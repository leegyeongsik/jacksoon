package io.jacksoon.router.init.factory;

import io.jacksoon.router.init.registration.InitInstance;
import io.jacksoon.router.init.registration.TypeMetadata;

import java.lang.reflect.TypeVariable;
import java.util.List;

public class TypeUtils {

    public static boolean match(TypeMetadata source, TypeMetadata target) {

        if (source == null || target == null) {
            return false;
        }

        if (target.getSourceType() instanceof TypeVariable<?>) {
            List<TypeMetadata> bounds = target.getUpperBounds();

            if (bounds.isEmpty()) {
                return true;
            }

            TypeMetadata bound = bounds.getFirst();

            if (bound.getRawType() == Object.class) {
                return true;
            }

            return bound.getRawType().isAssignableFrom(source.getRawType());
        }

        if (target.isWildcard()) {

            List<TypeMetadata> bounds = target.getUpperBounds();

            if (bounds.isEmpty()) {
                return true;
            }

            TypeMetadata bound = bounds.getFirst();

            return bound.getRawType().isAssignableFrom(source.getRawType());
        }

        if (!target.getRawType().isAssignableFrom(source.getRawType())) {
            return false;
        }

        if (!source.getRawType().equals(target.getRawType())) {
            return true;
        }

        List<TypeMetadata> sourceArgs = source.getActualTypeArguments();

        List<TypeMetadata> targetArgs = target.getActualTypeArguments();

        if (sourceArgs.size() != targetArgs.size()) {
            return false;
        }

        for (int i = 0; i < sourceArgs.size(); i++) {
            if (!match(sourceArgs.get(i), targetArgs.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static InitInstance findExactMatch(TypeMetadata request, List<InitInstance> candidates) {
        for (InitInstance candidate : candidates) {
            TypeMetadata candidateType = candidate.getInitMetadata().getTypeMetadata();
            if (request.equals(candidateType)) {
                return candidate;
            }
        }

        return null;
    }

    public static boolean moreSpecific(TypeMetadata left, TypeMetadata right) {
        if (left.equals(right)) {
            return false;
        }
        if (!left.isWildcard() && right.isWildcard()) {
            return true;
        }
        if (!(left.getSourceType() instanceof java.lang.reflect.TypeVariable)
                && right.getSourceType() instanceof java.lang.reflect.TypeVariable) {
            return true;
        }
        if (!left.getRawType().equals(right.getRawType())) {
            return false;
        }
        List<TypeMetadata> leftArgs = left.getActualTypeArguments();
        List<TypeMetadata> rightArgs = right.getActualTypeArguments();
        for (int i = 0; i < leftArgs.size(); i++) {
            if (moreSpecific(leftArgs.get(i), rightArgs.get(i))) {
                return true;
            }
        }

        return false;
    }

    public static InitInstance selectBestCandidate(TypeMetadata request, List<InitInstance> candidates) {
        InitInstance exact = findExactMatch(request, candidates);
        if (exact != null) {
            return exact;
        }
        InitInstance best = candidates.getFirst();

        for (InitInstance candidate : candidates) {
            TypeMetadata candidateType = candidate.getInitMetadata().getTypeMetadata();
            TypeMetadata bestType = best.getInitMetadata().getTypeMetadata();
            if (TypeUtils.moreSpecific(candidateType, bestType)) {
                best = candidate;
            }
        }
        return best;
    }


}