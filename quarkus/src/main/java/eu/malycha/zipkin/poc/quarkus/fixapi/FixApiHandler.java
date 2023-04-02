package eu.malycha.zipkin.poc.quarkus.fixapi;

import eu.malycha.zipkin.poc.quarkus.collider.ColliderHandler;
import eu.malycha.zipkin.poc.quarkus.gateway.GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class FixApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixApiHandler.class);

    @Inject
    GatewayHandler gatewayHandler;

    @Inject
    FixApiStorage fixApiStorage;

    void handle() {
        LOGGER.info("FixApiHandler.handle()");
        delay(100);
        fixApiStorage.storeOrderId();
        delay(150);
        gatewayHandler.handle();
    }

}
