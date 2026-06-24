package io.jacksoon.init.registration;


import io.jacksoon.init.factory.InitFactory;
import io.jacksoon.init.factory.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static io.jacksoon.init.factory.TypeUtils.match;

public class InitProcess {
    public static Object resolve(TypeMetadata metadata, String name) {
        if (name != null && !name.isEmpty()) {
            return resolveByName(metadata, name);
        }
        return resolveByType(metadata);
    }

    private static Object resolveByType(TypeMetadata metadata) {
        Class<?> raw = metadata.getRawType();
        if (raw == List.class) {
            TypeMetadata child = metadata.getActualTypeArguments().isEmpty() ?
                    new TypeMetadata(Object.class, Object.class, List.of(), false, List.of()) :
                    metadata.getActualTypeArguments().get(0);
            List<Object> result = new ArrayList<>();
            InitNode node = InitFactory.getInitNode(child.getRawType());
            List<InitInstance> candidates = collectCandidates(node);
            for (InitInstance instance : candidates) {
                TypeMetadata candidateType = instance.getInitMetadata().getTypeMetadata();
                if (match(candidateType, child)) {
                    result.add(instance.getInitMetadata().createInstance().getObject());
                }
            }
            return result;
        }
        InitNode node = InitFactory.getInitNode(raw);
        List<InitInstance> candidates = collectCandidates(node);

        List<InitInstance> confirmed = new ArrayList<>();

        for (InitInstance c : candidates) {
            TypeMetadata t = c.getInitMetadata().getTypeMetadata();

            if (match(t, metadata)) {
                confirmed.add(c);
            }
        }
        if (confirmed.isEmpty()) {
            throw new RuntimeException("No candidate: " + metadata);
        }
        return TypeUtils.selectBestCandidate(metadata, confirmed).getInitMetadata().createInstance().getObject();
    }
    private static Object resolveByName(TypeMetadata metadata, String name) {
        InitInstance instance = InitFactory.getInitInstance(name);
        if (instance == null) {
            throw new RuntimeException();
        }
        TypeMetadata candidate = instance.getInitMetadata().getTypeMetadata();
        if (!match(candidate, metadata)) {
            throw new RuntimeException();
        }
        return instance.getInitMetadata().createInstance().getObject();
    }
    public static List<InitInstance> collectCandidates(InitNode node) {
        List<InitInstance> result = new ArrayList<>();
        List<InitInstance> current = InitFactory.getTypeList(node.getClazz());
        if (current != null) {
            result.addAll(current);
        }
        for (InitNode child : node.getChildren()) {
            result.addAll(collectCandidates(child));
        }
        return result;
    }
}
