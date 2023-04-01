package eu.malycha.zipkin.poc.micrometer.service;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.tracing.test.simple.SimpleTracer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static io.micrometer.tracing.test.simple.TracerAssert.assertThat;

@SpringBootTest
@AutoConfigureObservability
class GreetingServiceTracingTest {

    @Autowired
    GreetingService greetingService;

    @Value("${management.tracing.enabled:true}")
    boolean tracingEnabled;

    @Test
    void testEnabledTracing() {
        Assertions.assertThat(tracingEnabled).isTrue();
    }

    @Autowired
    SimpleTracer simpleTracer;

    @Test
    void testTracing() {
        greetingService.sayHello();
        assertThat(simpleTracer)
            .onlySpan()
            .hasNameEqualTo("greeting-service")
            .isEnded();
    }

    @TestConfiguration
    static class ObservationTestConfiguration {

        @Bean
        TestObservationRegistry observationRegistry() {
            return TestObservationRegistry.create();
        }

        @Bean
        SimpleTracer simpleTracer() {
            return new SimpleTracer();
        }
    }
}
