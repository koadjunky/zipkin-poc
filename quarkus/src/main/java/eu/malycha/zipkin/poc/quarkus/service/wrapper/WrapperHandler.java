package eu.malycha.zipkin.poc.quarkus.service.wrapper;

import eu.malycha.zipkin.poc.quarkus.model.Order;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class WrapperHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperHandler.class);

    @Incoming("venue")
    public CompletionStage<Void> handle(Message<JsonObject> message) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("WrapperHandler.handle({})", order.orderId);
        delay(100);
        // Send message
        delay(100);
        // Receive answer
        delay(30);
        return message.ack();
    }
}
