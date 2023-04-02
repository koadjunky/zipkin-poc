package eu.malycha.zipkin.poc.quarkus.collider;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class ColliderStorage {

    void fetchSecurity() {
        delay(15);
        // Send message
        delay(25);
        // Receive answer
        delay(30);
        // Set status code ok
    }

    void fetchOrderState() {
        delay(20);
        // Send message
        delay(100);
        // Receive answer
        delay(30);
        // Set status code ok
    }

    void storeOrderState() {
        delay(120);
        // Set status ok
    }
}
