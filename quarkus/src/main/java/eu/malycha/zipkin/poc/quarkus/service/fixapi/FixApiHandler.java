package eu.malycha.zipkin.poc.quarkus.service.fixapi;

import eu.malycha.zipkin.poc.quarkus.infra.OpenTelemetryContext;
import eu.malycha.zipkin.poc.quarkus.model.Order;
import eu.malycha.zipkin.poc.quarkus.service.gateway.GatewayHandler;
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

    @Inject
    FixApiStorage fixApiStorage;

    @Channel("client-outgoing")
    Emitter<Order> orderEmitter;

    void handle() throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("fix-api-server")) {
            otc.runInSpan("request", "newOrder", () -> {
                otc.runInSpan("core", "newOrder", () -> {
                    handleInner();
                });
            });
        }
    }

    void handleInner() {
        LOGGER.info("FixApiHandler.handle()");
        delay(100);
        fixApiStorage.storeOrderId();
        delay(150);
        emit(UUID.randomUUID().toString());
    }

    void emit(String orderId) {
        LOGGER.info("FixApiHandler.emit({})", orderId);
        orderEmitter.send(new Order(orderId));
    }
}
