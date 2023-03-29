package eu.malycha.zipkin.poc;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BasicTracingTest {

    static OpenTelemetry openTelemetry;

    @BeforeAll
    static void beforeAll() {
        Resource inner = Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, "fix-api-server")
        );
        Resource resource = Resource.getDefault().merge(inner);

        SpanExporter spanExporter = JaegerThriftSpanExporter.builder()
            .build();

        SpanProcessor spanProcessor = SimpleSpanProcessor.create(spanExporter);

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(resource)
            .build();

        ContextPropagators propagators = ContextPropagators.create(
            W3CTraceContextPropagator.getInstance()
        );

        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(propagators)
            .buildAndRegisterGlobal();
    }

    @Test
    void basicTest() throws Exception {
        Tracer tracer = openTelemetry.getTracer("fix-api-server-core", "1.0.0");
        Span span = tracer.spanBuilder("login").startSpan();
        try (Scope ss = span.makeCurrent()) {
            Thread.sleep(2000);
        } finally {
            span.end();
        }
    }
}
