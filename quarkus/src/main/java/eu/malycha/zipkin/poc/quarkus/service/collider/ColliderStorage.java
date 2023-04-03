package eu.malycha.zipkin.poc.quarkus.service.collider;

import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class ColliderStorage {

    void fetchSecurity(Tracing tracer) {
        try (var ignored = tracer.createSpan("fetchSecurity")) {
            delay(15);
            Span.current().addEvent("Send message");
            delay(25);
            Span.current().addEvent("Receive answer");
            delay(30);
            Span.current().setStatus(StatusCode.OK);
        }
    }

    void fetchOrderState(Tracing tracer) {
        try (var ignored = tracer.createSpan("fetchOrderState")) {
            delay(20);
            Span.current().addEvent("Send message");
            delay(100);
            Span.current().addEvent("Receive answer");
            delay(30);
            Span.current().setStatus(StatusCode.OK);
        }
    }

    void storeOrderState(Tracing tracer) {
        try (var ignored = tracer.createSpan("storeOrderState")) {
            delay(120);
            Span.current().setStatus(StatusCode.OK);
        }
    }
}
