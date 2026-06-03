package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExactVsWildcardSelectionTest {

    static class Book {}
    static class TaskMapping<T> {}

    static class BookTaskQueue<T> {}

    @Init
    public static class WildcardQueue
            extends BookTaskQueue<TaskMapping<?>> {
    }

    @Init
    public static class ExactQueue
            extends BookTaskQueue<TaskMapping<Book>> {
    }

    @Init
    public static class Service {

        private final BookTaskQueue<TaskMapping<Book>> queue;

        public Service(BookTaskQueue<TaskMapping<Book>> queue) {
            this.queue = queue;
        }

        public BookTaskQueue<TaskMapping<Book>> getQueue() {
            return queue;
        }
    }

    @Test
    void exact_should_win_over_wildcard() {

        InitFactory.initialize(this.getClass());

        Service service =
                (Service)
                        InitFactory.getInitInstance("Service")
                                .getObject();

        assertNotNull(service);

        assertInstanceOf(
                ExactQueue.class,
                service.getQueue()
        );
    }
}