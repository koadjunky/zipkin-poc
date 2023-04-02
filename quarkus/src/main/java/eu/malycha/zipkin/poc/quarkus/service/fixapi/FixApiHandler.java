package eu.malycha.zipkin.poc.quarkus.service.fixapi;

import eu.malycha.zipkin.poc.quarkus.infra.OpenTelemetryContext;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import eu.malycha.zipkin.poc.quarkus.service.gateway.GatewayHandler;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
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
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("fix-api-server")) {
            otc.runInSpan("request", "newOrder", () -> {
                Baggage.current().toBuilder()
                    .put("clientId", CLIENT_ID)
                    .put("venueAccount", VENUE_ACCOUNT)
                    .put("clOrderId", clOrderId)
                    .build()
                    .storeInContext(Context.current())
                    .makeCurrent();
                otc.runInSpan("core", "newOrder", () -> {
                    handleInner();
                });
            });
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
