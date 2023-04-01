package eu.malycha.zipkin.poc.micrometer.service;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static io.micrometer.observation.tck.TestObservationRegistryAssert.assertThat;

@SpringBootTest
class GreetingServiceTest {

    @Autowired
    GreetingService greetingService;

    @Autowired
    TestObservationRegistry registry;

    @Test
    void testObservation() {
        // invoke service
        greetingService.sayHello();
        assertThat(registry)
            .hasObservationWithNameEqualTo("greetingService")
            .that()
            .hasBeenStarted()
            .hasBeenStopped();
    }

    @TestConfiguration
    static class ObservationTestConfiguration {

        @Bean
        TestObservationRegistry observationRegistry() {
            return TestObservationRegistry.create();
        }
    }
}
