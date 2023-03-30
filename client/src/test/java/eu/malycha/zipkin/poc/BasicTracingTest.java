package eu.malycha.zipkin.poc;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class BasicTracingTest {

    static final Duration DEFAULT = Duration.ofSeconds(1);

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
                otc.runInSpan("core", "newOrder", () -> {
                    delay(100);
                    otc.runInSpan("hazelcast", "storeOrderId", () -> {
                        delay(200);
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
                    Span.current().addEvent("Send message");
                    delay(150);
                    Span.current().addEvent("Receive answer");
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

    StringMapContext orderCollider(StringMapContext context) throws Exception {
        delay(44);
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("order-collider")) {
            return otc.runInRootSpan("core", "handleNewOrder", context, () -> {
                delay(DEFAULT);
            });
        }
    }

    void orderWrapper(StringMapContext context) throws Exception {
        delay(87);
        try (OpenTelemetryContext otc = OpenTelemetryContext.create("connector-wrapper")) {
            otc.runInRootSpan("core", "handleNewOrder", context, () -> {
                delay(DEFAULT);
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
