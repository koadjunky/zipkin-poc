package eu.malycha.zipkin.poc.quarkus.fixapi;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class FixApiStorage {

    void storeOrderId() {
        delay(200);
    }
}
