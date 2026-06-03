package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericInjectionIntegrationTest {

    static class Book {
    }

    static class User {
    }

    static class BookTaskQueue<T> {
    }

    @Init
    public static class BookQueue
            extends BookTaskQueue<Book> {
    }

    @Init
    public static class UserQueue
            extends BookTaskQueue<User> {
    }

    @Init
    public static class QueueService {

        private final BookTaskQueue<Book> queue;
        private final BookQueue bookQueue;
        public QueueService(
                BookTaskQueue<Book> queue, BookQueue bookQueue
        ) {
            this.queue = queue;
            this.bookQueue = bookQueue;
        }

        public BookTaskQueue<Book> getQueue() {
            return queue;
        }
        public BookQueue getBookQueue(){
            return bookQueue;
        }
    }

    @Test
    void generic_injection_test() {

        InitFactory.initialize(
                GenericInjectionIntegrationTest.class
        );

        System.out.println(
                "BookQueue metadata = " +
                        InitFactory
                                .getInitInstance("BookQueue")
                                .getInitMetadata()
                                .getTypeMetadata()
        );

        System.out.println(
                "UserQueue metadata = " +
                        InitFactory
                                .getInitInstance("UserQueue")
                                .getInitMetadata()
                                .getTypeMetadata()
        );

        QueueService service =
                (QueueService)
                        InitFactory
                                .getInitInstance("QueueService")
                                .getObject();

        assertNotNull(service);

        assertNotNull(
                service.getQueue()
        );
        assertNotNull(
                service.getBookQueue()
        );
        assertEquals(service.getQueue(),service.getBookQueue());
        System.out.println(
                "Injected type = " +
                        service.getQueue().getClass()
        );

        assertInstanceOf(
                BookQueue.class,
                service.getQueue()
        );
    }
}