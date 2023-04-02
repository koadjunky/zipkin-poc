package eu.malycha.zipkin.poc.quarkus.service.collider;

import eu.malycha.zipkin.poc.quarkus.service.wrapper.WrapperHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class ColliderHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColliderHandler.class);

    @Inject
    WrapperHandler wrapperHandler;

    @Inject
    ColliderStorage colliderStorage;

    public void handle() {
        LOGGER.info("ColliderHandler.handle()");
        delay(100);
        colliderStorage.fetchSecurity();
        delay(10);
        colliderStorage.fetchOrderState();
        delay(100);
        colliderStorage.storeOrderState();
        wrapperHandler.handle();
    }
}
