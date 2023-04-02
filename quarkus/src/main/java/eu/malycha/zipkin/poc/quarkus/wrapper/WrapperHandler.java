package eu.malycha.zipkin.poc.quarkus.wrapper;

import eu.malycha.zipkin.poc.quarkus.collider.ColliderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class WrapperHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperHandler.class);

    public void handle() {
        LOGGER.info("WrapperHandler.handle()");
        delay(100);
        // Send message
        delay(100);
        // Receive answer
        delay(30);
    }
}
