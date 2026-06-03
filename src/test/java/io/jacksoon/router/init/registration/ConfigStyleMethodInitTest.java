package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigStyleMethodInitTest {

    static class Book {}
    static class BookInfoRequest {}
    static class BookDeleteRequest {}
    static class TaskMapping<T> {}

    enum EventType {
        INSERT, UPDATE, SAVE, DELETE
    }

    static class BookTaskQueue<T> {
        Set<EventType> events;

        public BookTaskQueue(Set<EventType> events) {
            this.events = events;
        }
    }

    @Init
    public static class BookTaskQueueConfig {

        @Init(value = "bookEventQueue")
        public BookTaskQueue<TaskMapping<?>> bookEventQueue() {
            return new BookTaskQueue<>(new LinkedHashSet<>());
        }

        public BookTaskQueue<TaskMapping<BookInfoRequest>> bookEmbeddingQueue() {
            return new BookTaskQueue<>(Set.of(EventType.INSERT, EventType.UPDATE));
        }

        @Init(value = "bookSaveQueue")
        public BookTaskQueue<TaskMapping<Book>> bookSaveQueue() {
            return new BookTaskQueue<>(Set.of(EventType.SAVE));
        }

        @Init(value = "bookDeleteQueue")
        public BookTaskQueue<TaskMapping<BookDeleteRequest>> bookDeleteQueue() {
            return new BookTaskQueue<>(Set.of(EventType.DELETE));
        }
    }

    @Test
    void config_method_beans_should_be_resolved() {

        InitFactory.initialize(this.getClass());

        assertNotNull(InitFactory.getInitInstance("bookEventQueue").getObject());
        assertNotNull(InitFactory.getInitInstance("bookSaveQueue").getObject());
        assertNotNull(InitFactory.getInitInstance("bookDeleteQueue").getObject());
    }
}