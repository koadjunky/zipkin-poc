package eu.malycha.zipkin.poc.quarkus.service.gateway;

import eu.malycha.zipkin.poc.quarkus.service.collider.ColliderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class GatewayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayHandler.class);

    @Inject
    ColliderHandler colliderHandler;

    @Inject
    GatewayStorage gatewayStorage;

    public void handle() {
        LOGGER.info("GatewayHandler.handle()");
        delay(100);
        gatewayStorage.fetchOrderState();
        delay(100);
        gatewayStorage.storeOrderState();
        colliderHandler.handle();
        // Set status ERROR
        // Set exception
    }
}
