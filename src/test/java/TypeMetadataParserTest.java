import io.jacksoon.router.init.registration.TypeMetadata;
import io.jacksoon.router.init.registration.TypeMetadataParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class TypeMetadataParserTest {

    static class ABC {
    }

    static class BookTaskQueue<T> {
    }

    static class Registry<T extends ABC> {

        private List<BookTaskQueue<T>> queues;
    }

    @Test
    void parse_nested_type_variable() throws Exception {

        Field field =
                Registry.class.getDeclaredField("queues");

        TypeMetadata metadata =
                TypeMetadataParser.parse(field.getGenericType());

        System.out.println("root");
        print(metadata, 0);
    }

    private static void print(TypeMetadata metadata, int depth) {

        String indent = "  ".repeat(depth);

        System.out.println(indent + "sourceType = "
                + metadata.getSourceType());

        System.out.println(indent + "rawType = "
                + metadata.getRawType());

        System.out.println(indent + "wildcard = "
                + metadata.isWildcard());

        System.out.println(indent + "args = "
                + metadata.getActualTypeArguments().size());

        System.out.println(indent + "bounds = "
                + metadata.getUpperBounds().size());

        for (TypeMetadata bound : metadata.getUpperBounds()) {
            System.out.println(indent + "BOUND ->");
            print(bound, depth + 1);
        }

        for (TypeMetadata arg : metadata.getActualTypeArguments()) {
            System.out.println(indent + "ARG ->");
            print(arg, depth + 1);
        }
    }
}