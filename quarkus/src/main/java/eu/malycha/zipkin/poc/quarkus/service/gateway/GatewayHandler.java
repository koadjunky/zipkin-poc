package eu.malycha.zipkin.poc.quarkus.service.gateway;

import eu.malycha.zipkin.poc.quarkus.infra.TelemetryUtil;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import eu.malycha.zipkin.poc.quarkus.infra.otl.RabbitHeadersPropagator;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
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
            Context parent = tracing.loadContext(RabbitHeadersPropagator.create(message.getHeaders()), RabbitHeadersPropagator.getter());
            try (var ignored2 = tracing.createBaggage(parent)) {
                try (var ignored = tracing.createSpan("handleNewOrder", parent)) {
                    return handleInner(message, tracing);
                }
            }
        }
    }

    public CompletionStage<Void> handleInner(Message<JsonObject> message, Tracing tracing) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("GatewayHandler.handle({})", order.orderId);
        delay(100);
        gatewayStorage.fetchOrderState(tracing);
        delay(100);
        gatewayStorage.storeOrderState(tracing);
        emit(order, tracing);
        Span.current().setStatus(StatusCode.ERROR);
        try {
            throw new Exception("Example exception");
        } catch (Exception ex) {
            Span.current().recordException(ex);
        }
        return message.ack();
    }

    void emit(Order order, Tracing tracing) {
        LOGGER.info("GatewayHandler.emit({})", order.orderId);
        ServiceUtils.emit(orderEmitter, order, tracing);
    }
}
