package eu.malycha.zipkin.poc.quarkus.infra.otl;

import eu.malycha.zipkin.poc.quarkus.infra.SafeCloseable;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class OpenTelemetryTracing implements Tracing {

    private final String defaultScope;
    private final String defaultVersion;

    private final SdkTracerProvider tracerProvider;
    private final OpenTelemetrySdk openTelemetry;

    private OpenTelemetryTracing(String serviceName, String defaultScope, String defaultVersion) {
        this.defaultScope = defaultScope;
        this.defaultVersion = defaultVersion;
        this.tracerProvider = createTracerProvider(serviceName);
        this.openTelemetry = createOpenTelemetry(tracerProvider);
    }

    public static OpenTelemetryTracing create(String serviceName, String defaultScope, String defaultVersion) {
        return new OpenTelemetryTracing(serviceName, defaultScope, defaultVersion);
    }

    @Override
    @SuppressWarnings("EmptyTryBlock")
    public void close() throws IOException {
        try (Closeable tp = tracerProvider) {
            // Empty
        }
    }

    public SafeCloseable createSpan(String spanName) {
        return createSpan(defaultScope, defaultVersion, spanName, null);
    }

    public SafeCloseable createSpan(String spanName, Context parent) {
        return createSpan(defaultScope, defaultVersion, spanName, parent);
    }

    public SafeCloseable createSpan(String scopeName, String version, String spanName) {
        return createSpan(scopeName, version, spanName, null);
    }

    public SafeCloseable createSpan(String scopeName, String version, String spanName, Context parent) {
        Tracer tracer = openTelemetry.getTracer(scopeName, version);
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName);
        if (parent != null) {
            spanBuilder.setParent(parent);
        }
        return new SpanWrapper(spanBuilder.startSpan());
    }

    public SafeCloseable createBaggage(Context context) {
        return new BaggageWrapper(Baggage.fromContext(context));
    }

    public SafeCloseable createBaggage(Map<String, String> content) {
        BaggageBuilder baggageBuilder = Baggage.current().toBuilder();
        content.forEach(baggageBuilder::put);
        return new BaggageWrapper(baggageBuilder.build());
    }

    public <C> Context loadContext(C propagator, TextMapGetter<C> getter) {
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
        return textMapPropagator.extract(Context.current(), propagator, getter);
    }

    public <C> C saveContext(C propagator, TextMapSetter<C> setter) {
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
        textMapPropagator.inject(Context.current(), propagator, setter);
        return propagator;
    }

    private static SdkTracerProvider createTracerProvider(String serviceName) {
        Resource inner = Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)
        );
        Resource resource = Resource.getDefault().merge(inner);

        // otel-collector
        SpanExporter otelCollectorSpanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .build();

        SpanProcessor spanProcessor = SpanProcessor.composite(
            BatchSpanProcessor.builder(otelCollectorSpanExporter).build(),
            BaggageSpanProcessor.create()
        );

        SdkTracerProviderBuilder sdkTracerProviderBuilder = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(resource);
        return sdkTracerProviderBuilder.build();
    }

    private static OpenTelemetrySdk createOpenTelemetry(SdkTracerProvider sdkTracerProvider) {
        ContextPropagators propagators = ContextPropagators.create(
            TextMapPropagator.composite(
                W3CTraceContextPropagator.getInstance(),
                W3CBaggagePropagator.getInstance()
            )
        );
        OpenTelemetrySdkBuilder openTelemetryBuilder = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(propagators);
        return openTelemetryBuilder.build();
    }
}
