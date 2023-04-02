package eu.malycha.zipkin.poc.quarkus.service.gateway;

import eu.malycha.zipkin.poc.quarkus.model.Order;
import eu.malycha.zipkin.poc.quarkus.service.collider.ColliderHandler;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class GatewayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayHandler.class);

    @Channel("oems-outgoing")
    Emitter<Order> orderEmitter;

    @Inject
    GatewayStorage gatewayStorage;

    @Incoming("client")
    public CompletionStage<Void> handle(Message<JsonObject> message) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("GatewayHandler.handle({})", order.orderId);
        delay(100);
        gatewayStorage.fetchOrderState();
        delay(100);
        gatewayStorage.storeOrderState();
        emit(order);
        // Set status ERROR
        // Set exception
        return message.ack();
    }

    void emit(Order order) {
        LOGGER.info("GatewayHandler.emit({})", order.orderId);
        orderEmitter.send(order);
    }
}
