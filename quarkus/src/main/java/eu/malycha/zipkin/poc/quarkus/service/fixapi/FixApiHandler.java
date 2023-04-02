package eu.malycha.zipkin.poc.quarkus.service.fixapi;

import eu.malycha.zipkin.poc.quarkus.infra.OpenTelemetryContext;
import eu.malycha.zipkin.poc.quarkus.service.gateway.GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class FixApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixApiHandler.class);

    @Inject
    GatewayHandler gatewayHandler;

    @Inject
    FixApiStorage fixApiStorage;

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
        gatewayHandler.handle();
    }

}
