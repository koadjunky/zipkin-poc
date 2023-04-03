package eu.malycha.zipkin.poc.quarkus.service.gateway;

import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class GatewayStorage {

    void fetchOrderState(Tracing tracing) {
        try (var ignored = tracing.createSpan("fetchOrderState")) {
            delay(20);
            Span.current().addEvent("Send message");
            delay(100);
            Span.current().addEvent("Receive answer");
            delay(30);
            Span.current().setStatus(StatusCode.OK);
        }
    }

    void storeOrderState(Tracing tracing) {
        try (var ignored = tracing.createSpan("storeOrderState")) {
            delay(120);
            Span.current().setStatus(StatusCode.OK);
        }
    }
}
