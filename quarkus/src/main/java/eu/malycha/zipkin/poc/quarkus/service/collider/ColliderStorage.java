package eu.malycha.zipkin.poc.quarkus.service.collider;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import javax.enterprise.context.ApplicationScoped;

import static eu.malycha.zipkin.poc.quarkus.utils.ServiceUtils.delay;

@ApplicationScoped
public class ColliderStorage {

    void fetchSecurity(Tracer tracer) {
        Span span = tracer.spanBuilder("fetchSecurity").startSpan();
        try (Scope ss = span.makeCurrent()) {
            delay(15);
            Span.current().addEvent("Send message");
            delay(25);
            Span.current().addEvent("Receive answer");
            delay(30);
            Span.current().setStatus(StatusCode.OK);
        } finally {
            span.end();
        }
    }

    void fetchOrderState(Tracer tracer) {
        Span span = tracer.spanBuilder("fetchOrderState").startSpan();
        try (Scope ss = span.makeCurrent()) {
            delay(20);
            Span.current().addEvent("Send message");
            delay(100);
            Span.current().addEvent("Receive answer");
            delay(30);
            Span.current().setStatus(StatusCode.OK);
        } finally {
            span.end();
        }
    }

    void storeOrderState(Tracer tracer) {
        Span span = tracer.spanBuilder("storeOrderState").startSpan();
        try (Scope ss = span.makeCurrent()) {
            delay(120);
            Span.current().setStatus(StatusCode.OK);
        } finally {
            span.end();
        }
    }
}
