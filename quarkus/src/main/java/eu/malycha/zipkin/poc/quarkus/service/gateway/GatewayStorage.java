package eu.malycha.zipkin.poc.quarkus.service.gateway;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class GatewayStorage {

    void fetchOrderState() {
        delay(20);
        // Send message
        delay(100);
        // Receive message
        delay(30);
        // Set status OK
    }

    void storeOrderState() {
        delay(120);
        // Set status OK
    }
}
