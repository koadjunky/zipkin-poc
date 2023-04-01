package eu.malycha.zipkin.poc.micrometer.infra;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ObservationApiConfiguration {

    //@Bean
    public ObservationRegistry createObservationRegistry() {
        return ObservationRegistry.create();
    }
}
