package io.jacksoon.init.registration;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListInjectionIntegrationTest {

    interface Handler {
    }

    @Init
    public static class BookHandler
            implements Handler {
    }

    @Init
    public static class UserHandler
            implements Handler {
    }

    @Init
    public static class HandlerService {

        private final List<Handler> handlers;

        public HandlerService(List<Handler> handlers) {
            this.handlers = handlers;
        }

        public List<Handler> getHandlers() {
            return handlers;
        }
    }

    @Test
    void list_injection_test() {

        InitFactory.initialize(this.getClass());

        HandlerService service =
                (HandlerService)
                        InitFactory
                                .getInitInstance("HandlerService")
                                .getObject();

        assertNotNull(service);

        assertEquals(
                2,
                service.getHandlers().size()
        );
    }
}