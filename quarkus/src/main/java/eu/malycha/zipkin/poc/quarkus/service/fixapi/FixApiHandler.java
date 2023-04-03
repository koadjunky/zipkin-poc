package eu.malycha.zipkin.poc.quarkus.service.fixapi;

import eu.malycha.zipkin.poc.quarkus.infra.TelemetryUtil;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class FixApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixApiHandler.class);

    static final String CLIENT_ID = "Greebo";
    static final String VENUE_ACCOUNT = "MadStoat";


    @Inject
    FixApiStorage fixApiStorage;

    @Channel("client-outgoing")
    Emitter<Order> orderEmitter;

    void handle() throws Exception {
        String clOrderId = UUID.randomUUID().toString();
        try (Tracing tracing = TelemetryUtil.createTracing("fix-api-server", "core", "1.0.0")) {
            try (var ignored = tracing.createSpan("request", "1.0.0", "newOrder")) {
                Baggage.current().toBuilder()
                    .put("clientId", CLIENT_ID)
                    .put("venueAccount", VENUE_ACCOUNT)
                    .put("clOrderId", clOrderId)
                    .build()
                    .storeInContext(Context.current())
                    .makeCurrent(); // TODO: createBaggage
                try (var ignored2 = tracing.createSpan("newOrder")) {
                    handleInner();
                }
            }
        }
    }

    void handleInner() {
        LOGGER.info("FixApiHandler.handle()");
        delay(100);
        String orderId = UUID.randomUUID().toString();
        fixApiStorage.storeOrderId();
        Baggage.current().toBuilder()
            .put("orderId", orderId)
            .build().makeCurrent();
        delay(150);
        emit(orderId);
    }

    void emit(String orderId) {
        LOGGER.info("FixApiHandler.emit({})", orderId);
        orderEmitter.send(new Order(orderId));
    }
}
