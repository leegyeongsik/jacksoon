package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CandidateSelectionIntegrationTest {

    static class Book {
    }

    static class BookTaskQueue<T> {
    }

    @Init
    public static class ExactBookQueue
            extends BookTaskQueue<Book> {
    }

    @Init
    public static class WildcardBookQueue
            extends BookTaskQueue<Object> {
    }

    @Init
    public static class QueueService {

        private final BookTaskQueue<Book> queue;

        public QueueService(
                BookTaskQueue<Book> queue) {
            this.queue = queue;
        }

        public BookTaskQueue<Book> getQueue() {
            return queue;
        }
    }

    @Test
    void exact_candidate_should_win() {

        InitFactory.initialize(
                CandidateSelectionIntegrationTest.class
        );

        QueueService service =
                (QueueService)
                        InitFactory
                                .getInitInstance("QueueService")
                                .getObject();

        System.out.println(
                "Injected = " +
                        service.getQueue().getClass()
        );

        assertInstanceOf(
                ExactBookQueue.class,
                service.getQueue()
        );
    }
}