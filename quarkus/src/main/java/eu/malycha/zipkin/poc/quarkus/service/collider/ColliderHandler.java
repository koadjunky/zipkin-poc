package eu.malycha.zipkin.poc.quarkus.service.collider;

import eu.malycha.zipkin.poc.quarkus.infra.OpenTelemetryContext;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import eu.malycha.zipkin.poc.quarkus.service.wrapper.WrapperHandler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
public class ColliderHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColliderHandler.class);

    @Channel("venue-outgoing")
    Emitter<Order> orderEmitter;

    @Inject
    ColliderStorage colliderStorage;

    @Incoming("oems")
    public CompletionStage<Void> handle(Message<JsonObject> message) throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-collider")) {
            Tracer tracer = otc.getOpenTelemetry().getTracer("core", OpenTelemetryContext.VERSION);
            Span span = tracer.spanBuilder("handleNewOrder").startSpan();
            try (Scope ss = span.makeCurrent()) {
                return handleInner(message, tracer);
            } finally {
                span.end();
            }
        }
    }

    public CompletionStage<Void> handleInner(Message<JsonObject> message, Tracer tracer) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("ColliderHandler.handle({})", order.orderId);
        delay(100);
        colliderStorage.fetchSecurity(tracer);
        delay(10);
        colliderStorage.fetchOrderState(tracer);
        delay(100);
        colliderStorage.storeOrderState(tracer);
        emit(order);
        return message.ack();
    }

    void emit(Order order) {
        LOGGER.info("ColliderHandler.emit({})", order.orderId);
        orderEmitter.send(order);
    }
}
