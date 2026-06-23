//package io.jacksoon.router.pipeline.step;
//
//import io.jacksoon.router.pipeline.executor.PipeLineExecutor;
//import io.jacksoon.router.pipeline.executor.paser.HttpParse;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//
//class StepRegistryTest {
//
//    @Test
//    void shouldRegisterReadEventWithHttpParseAndNullNextEventByDefault() {
//        StepRegistry registry = new StepRegistry();
//
//        PipeLineExecutor executor = registry.getPipeLineExecutor("READ");
//        String nextEvent = registry.getPipelineStep("READ");
//
//        assertNotNull(executor);
//        assertInstanceOf(HttpParse.class, executor);
//        assertNull(nextEvent);
//    }
//
//    @Test
//    void shouldReturnRegisteredExecutorWhenPutIsCalled() {
//        StepRegistry registry = new StepRegistry();
//        PipeLineExecutor executor = mock(PipeLineExecutor.class);
//
//        registry.put("PARSE", executor, "ROUTE");
//
//        assertSame(executor, registry.getPipeLineExecutor("PARSE"));
//    }
//
//    @Test
//    void shouldReturnRegisteredNextEventWhenPutIsCalled() {
//        StepRegistry registry = new StepRegistry();
//        PipeLineExecutor executor = mock(PipeLineExecutor.class);
//
//        registry.put("PARSE", executor, "ROUTE");
//
//        assertEquals("ROUTE", registry.getPipelineStep("PARSE"));
//    }
//
//    @Test
//    void shouldThrowNullPointerExceptionWhenUnknownEventIsRequested() {
//        StepRegistry registry = new StepRegistry();
//
//        assertThrows(
//                NullPointerException.class,
//                () -> registry.getPipeLineExecutor("UNKNOWN")
//        );
//
//        assertThrows(
//                NullPointerException.class,
//                () -> registry.getPipelineStep("UNKNOWN")
//        );
//    }
//}