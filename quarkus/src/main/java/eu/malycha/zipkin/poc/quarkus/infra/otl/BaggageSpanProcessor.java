package eu.malycha.zipkin.poc.quarkus.infra.otl;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

class BaggageSpanProcessor implements SpanProcessor {

    static BaggageSpanProcessor create() {
        return new BaggageSpanProcessor();
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        Baggage.fromContext(parentContext)
            .forEach((s, baggageEntry) -> span.setAttribute(s, baggageEntry.getValue()));
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
