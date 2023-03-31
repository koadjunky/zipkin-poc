package eu.malycha.zipkin.poc;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.BitSet;
import java.util.UUID;

class BasicTracingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicTracingTest.class);

    static final Duration DEFAULT = Duration.ofSeconds(1);

    static final String CLIENT_ID = "Greebo";
    static final String VENUE_ACCOUNT = "MadStoat";
    static final String CL_ORDER_ID = UUID.randomUUID().toString();
    static final String ORDER_ID = UUID.randomUUID().toString();

    @Test
    void basicTest() throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("fix-api-server")) {
            otc.runInSpan("quickfixj", "login", () -> {
                delay(DEFAULT);
            });
        }
    }

    @Test
    void orderTest() throws Exception {
        StringMapContext context = orderFixApiServer();
        orderGateway(context);
        orderCollider(context);
        orderWrapper(context);
    }

    @Test
    void reportTest() throws Exception {
        StringMapContext context = reportWrapper();
        context = reportCollider(context);
        context = reportGateway(context);
        reportFixApiServer(context);
    }

    StringMapContext orderFixApiServer() throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("fix-api-server")) {
            return otc.runInSpan("request", "newOrder", () -> {
                LOGGER.info("fix-api-server context: {}", Context.current());
                Scope ss = Baggage.current().toBuilder()
                    .put("clientId", CLIENT_ID)
                    .put("venueAccount", VENUE_ACCOUNT)
                    .put("clOrderId", CL_ORDER_ID)
                    .build()
                    .storeInContext(Context.current())
                    .makeCurrent();
                otc.runInSpan("core", "newOrder", () -> {
                    LOGGER.info("fix-api-server newOrder context: {}", Context.current());
                    delay(100);
                    otc.runInSpan("hazelcast", "storeOrderId", () -> {
                        delay(200);
                        Scope ss1 = Baggage.current().toBuilder()
                            .put("orderId", ORDER_ID)
                            .build().makeCurrent();
                    });
                    delay(150);
                });
            });
        }
    }

    StringMapContext orderGateway(StringMapContext context) throws Exception {
        delay(85);
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-gateway")) {
            return otc.runInRootSpan("core", "handleNewOrder", context, () -> {
                delay(100);
                otc.runInSpan("hazelcast", "fetchOrderState", () -> {
                    delay(20);
                    Span.current().addEvent("Send message");
                    delay(100);
                    Span.current().addEvent("Receive answer");
                    delay(30);
                    Span.current().setStatus(StatusCode.OK);
                });
                delay(100);
                otc.runInSpan("hazelcast", "storeOrderState", () -> {
                    delay(120);
                    Span.current().setStatus(StatusCode.OK);
                });
                otc.runInSpan("rabbit", "emitMessage", () -> {
                    delay(25);
                    Span.current().setStatus(StatusCode.ERROR);
                    try {
                        throw new Exception("Example exception");
                    } catch (Exception ex) {
                        Span.current().recordException(ex);
                    }
                });
            });
        }
    }

    StringMapContext orderCollider(StringMapContext context) throws Exception {
        delay(44);
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-collider")) {
            return otc.runInRootSpan("core", "handleNewOrder", context, () -> {
                delay(100);
                otc.runInSpan("hazelcast", "fetchSecurity", () -> {
                    delay(15);
                    Span.current().addEvent("Send message");
                    delay(25);
                    Span.current().addEvent("Receive answer");
                    delay(30);
                    Span.current().setStatus(StatusCode.OK);
                });
                otc.runInSpan("hazelcast", "fetchOrderState", () -> {
                    delay(20);
                    Span.current().addEvent("Send message");
                    delay(100);
                    Span.current().addEvent("Receive answer");
                    delay(30);
                    Span.current().setStatus(StatusCode.OK);
                });
                delay(100);
                otc.runInSpan("hazelcast", "storeOrderState", () -> {
                    delay(120);
                    Span.current().setStatus(StatusCode.OK);
                });
                otc.runInSpan("rabbit", "emitMessage", () -> {
                    delay(25);
                    Span.current().setStatus(StatusCode.OK);
                });
            });
        }
    }

    void orderWrapper(StringMapContext context) throws Exception {
        delay(87);
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("connector-wrapper")) {
            otc.runInRootSpan("core", "handleNewOrder", context, () -> {
                delay(100);
                Span.current().addEvent("Send message");
                delay(100);
                Span.current().addEvent("Receive answer");
                delay(30);
            });
        }
    }

    StringMapContext reportWrapper() throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("connector-wrapper")) {
            return otc.runInSpan("report", "executionReport", () -> {
                otc.runInSpan("core", "connectorHandler", () -> {
                    delay(DEFAULT);
                });
            });
        }
    }

    StringMapContext reportCollider(StringMapContext context) throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-collider")) {
            return otc.runInRootSpan("core", "reportHandler", context, () -> {
                delay(DEFAULT);
            });
        }
    }

    StringMapContext reportGateway(StringMapContext context) throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-gateway")) {
            return otc.runInRootSpan("core", "reportHandler", context, () -> {
                delay(DEFAULT);
            });
        }
    }

    void reportFixApiServer(StringMapContext context) throws Exception {
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("fix-api-server")) {
            otc.runInRootSpan("core", "reportHandler", context, () -> {
                delay(DEFAULT);
            });
        }
    }


    public void delay(Duration duration) {
        delay(duration.toMillis());
    }

    public void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
