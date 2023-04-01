package eu.malycha.zipkin.poc.micrometer.service;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private final ObservationRegistry observationRegistry;

    GreetingService(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public String sayHello() {
        return Observation
            .createNotStarted("greetingService", observationRegistry)
            .observe(this::sayHelloNoObserver);
    }

    private String sayHelloNoObserver() {
        return "Hello World!";
    }
}
