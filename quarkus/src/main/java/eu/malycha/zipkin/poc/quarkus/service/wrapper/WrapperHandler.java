package eu.malycha.zipkin.poc.quarkus.service.wrapper;

import eu.malycha.zipkin.poc.quarkus.infra.OpenTelemetryContext;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
    public CompletionStage<Void> handle(Message<JsonObject> message) throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("connector-wrapper")) {
            Tracer tracer = otc.getOpenTelemetry().getTracer("core", OpenTelemetryContext.VERSION);
            Span span = tracer.spanBuilder("handleNewOrder").startSpan();
            try (Scope ss = span.makeCurrent()) {
                return handleInner(message);
            } finally {
                span.end();
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
