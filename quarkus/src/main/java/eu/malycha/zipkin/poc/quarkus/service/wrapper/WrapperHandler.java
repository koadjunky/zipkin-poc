package eu.malycha.zipkin.poc.quarkus.service.wrapper;

import eu.malycha.zipkin.poc.quarkus.infra.TelemetryUtil;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import eu.malycha.zipkin.poc.quarkus.infra.otl.RabbitHeadersPropagator;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage;
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
    public CompletionStage<Void> handle(IncomingRabbitMQMessage<JsonObject> message) throws Exception {
        try (Tracing tracing = TelemetryUtil.createTracing("connector-wrapper", "core", "1.0.0")) {
            Context parent = tracing.loadContext(RabbitHeadersPropagator.create(message.getHeaders()), RabbitHeadersPropagator.getter());
            try (var ignored2 = tracing.createBaggage(parent)) {
                try (var ignored = tracing.createSpan("handleNewOrder", parent)) {
                    return handleInner(message);
                }
            }
        }
    }

    public CompletionStage<Void> handleInner(Message<JsonObject> message) {
        Order order = message.getPayload().mapTo(Order.class);
        LOGGER.info("WrapperHandler.handle({})", order.orderId);
        delay(100);
        Span.current().addEvent("Send message");
        delay(100);
        Span.current().addEvent("Receive answer");
        delay(30);
        return message.ack();
    }
}
