package eu.malycha.zipkin.poc.quarkus.service.gateway;

import eu.malycha.zipkin.poc.quarkus.infra.TelemetryUtil;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage;
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
    public CompletionStage<Void> handle(IncomingRabbitMQMessage<JsonObject> message) throws Exception {
        try (Tracing tracing = TelemetryUtil.createTracing("order-gateway", "core", "1.0.0")) {
            try (var ignored = tracing.createSpan("handleNewOrder")) {
                return handleInner(message, tracing);
            }
        }
    }

    public CompletionStage<Void> handleInner(Message<JsonObject> message, Tracing tracer) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("GatewayHandler.handle({})", order.orderId);
        delay(100);
        gatewayStorage.fetchOrderState(tracer);
        delay(100);
        gatewayStorage.storeOrderState(tracer);
        emit(order);
        Span.current().setStatus(StatusCode.ERROR);
        try {
            throw new Exception("Example exception");
        } catch (Exception ex) {
            Span.current().recordException(ex);
        }
        return message.ack();
    }

    void emit(Order order) {
        LOGGER.info("GatewayHandler.emit({})", order.orderId);
        orderEmitter.send(order);
    }
}
