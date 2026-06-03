package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InitFactoryIntegrationTest {

    @Init
    public static class BookRepository {
    }

    @Init
    public static class BookService {

        private final BookRepository repository;

        public BookService(BookRepository repository) {
            this.repository = repository;
        }

        public BookRepository getRepository() {
            return repository;
        }
    }

    @Test
    void initialize_should_create_simple_object() {

        InitFactory.initialize(this.getClass());

        Object repository =
                InitFactory.getInitInstance("BookRepository")
                        .getObject();

        assertNotNull(repository);
        assertInstanceOf(
                BookRepository.class,
                repository
        );
    }

    @Test
    void initialize_should_inject_constructor_dependency() {

        InitFactory.initialize(this.getClass());

        BookService service =
                (BookService)
                        InitFactory.getInitInstance("BookService")
                                .getObject();

        assertNotNull(service);

        assertNotNull(
                service.getRepository()
        );

        assertInstanceOf(
                BookRepository.class,
                service.getRepository()
        );
    }
}