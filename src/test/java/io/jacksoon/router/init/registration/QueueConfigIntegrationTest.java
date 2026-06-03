package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QueueConfigIntegrationTest {

    static class Book {}
    static class User {}

    interface TaskMapping<T> {}

    enum EventType {
        INSERT, UPDATE, SAVE, DELETE
    }

    static class BookTaskQueue<T> {

        private final Set<EventType> supportEventType;

        public BookTaskQueue(Set<EventType> supportEventType) {
            this.supportEventType = supportEventType;
        }

        public List<EventType> getSupportEventType() {
            return List.copyOf(supportEventType);
        }
    }

    @Init
    public static class BookEventQueue
            extends BookTaskQueue<TaskMapping<?>> {

        public BookEventQueue() {
            super(Set.of(EventType.INSERT));
        }
    }

    @Init
    public static class BookEmbeddingQueue
            extends BookTaskQueue<TaskMapping<Book>> {

        public BookEmbeddingQueue() {
            super(Set.of(EventType.UPDATE));
        }
    }

    @Init
    public static class BookSaveQueue
            extends BookTaskQueue<TaskMapping<Book>> {

        public BookSaveQueue() {
            super(Set.of(EventType.SAVE));
        }
    }

    @Init
    public static class BookDeleteQueue
            extends BookTaskQueue<TaskMapping<Book>> {

        public BookDeleteQueue() {
            super(Set.of(EventType.DELETE));
        }
    }

    @Init
    public static class BookTaskQueueRegistry<T> {

        private final Map<EventType, BookTaskQueue<T>> registry =
                new EnumMap<>(EventType.class);

        public BookTaskQueueRegistry(List<BookTaskQueue<T>> queues) {

            for (BookTaskQueue<T> queue : queues) {
                for (EventType type : queue.getSupportEventType()) {
                    registry.put(type, queue);
                }
            }
        }

        public BookTaskQueue<T> getQueue(EventType type) {
            return registry.get(type);
        }

        public Map<EventType, BookTaskQueue<T>> getRawRegistry() {
            return registry;
        }
    }

    @Test
    void should_wire_all_queues_and_build_correct_event_registry() {

        InitFactory.initialize(this.getClass());

        BookTaskQueueRegistry<?> registry =
                (BookTaskQueueRegistry<?>)
                        InitFactory.getInitInstance("BookTaskQueueRegistry")
                                .getObject();

        assertNotNull(registry);

        Map<EventType, ?> map = registry.getRawRegistry();

        assertEquals(4, map.size());

        assertTrue(map.containsKey(EventType.INSERT));
        assertTrue(map.containsKey(EventType.UPDATE));
        assertTrue(map.containsKey(EventType.SAVE));
        assertTrue(map.containsKey(EventType.DELETE));

        map.forEach((k, v) -> assertNotNull(v));

        long distinctQueues = map.values()
                .stream()
                .distinct()
                .count();

        assertTrue(distinctQueues >= 3);

        assertInstanceOf(
                BookEventQueue.class,
                registry.getQueue(EventType.INSERT)
        );

        assertInstanceOf(
                BookEmbeddingQueue.class,
                registry.getQueue(EventType.UPDATE)
        );

        assertInstanceOf(
                BookSaveQueue.class,
                registry.getQueue(EventType.SAVE)
        );

        assertInstanceOf(
                BookDeleteQueue.class,
                registry.getQueue(EventType.DELETE)
        );
    }
}