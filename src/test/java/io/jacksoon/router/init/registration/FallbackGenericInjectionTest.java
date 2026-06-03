package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallbackGenericInjectionTest {

    static class Book {}
    static class TaskMapping<T> {}
    static class BookTaskQueueRegistry<T> {}

    @Init
    public static class DefaultRegistry
            extends BookTaskQueueRegistry<TaskMapping<?>> {
    }

    @Init
    public static class BookEmbeddingThreadPool {

        private final BookTaskQueueRegistry<TaskMapping<Book>> registry;

        public BookEmbeddingThreadPool(
                BookTaskQueueRegistry<TaskMapping<Book>> registry
        ) {
            this.registry = registry;
        }

        public BookTaskQueueRegistry<TaskMapping<Book>> getRegistry() {
            return registry;
        }
    }

    @Test
    void should_fallback_to_raw_registry_when_generic_missing() {

        InitFactory.initialize(this.getClass());

        BookEmbeddingThreadPool pool =
                (BookEmbeddingThreadPool)
                        InitFactory.getInitInstance("BookEmbeddingThreadPool")
                                .getObject();

        assertNotNull(pool);
        assertNotNull(pool.getRegistry());
        assertInstanceOf(DefaultRegistry.class, pool.getRegistry());
    }
}