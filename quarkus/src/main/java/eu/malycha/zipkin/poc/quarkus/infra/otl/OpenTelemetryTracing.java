package eu.malycha.zipkin.poc.quarkus.infra.otl;

import eu.malycha.zipkin.poc.quarkus.infra.SafeCloseable;
import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
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

public class OpenTelemetryTracing implements Tracing {

    public static final String VERSION = "1.0.0";

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
        return createSpan(defaultScope, defaultVersion, spanName);
    }

    public SafeCloseable createSpan(String scopeName, String version, String spanName) {
        Tracer tracer = openTelemetry.getTracer(scopeName, version);
        Span span = tracer.spanBuilder(spanName).startSpan();
        return new SpanWrapper(span);
    }

    private static SdkTracerProvider createTracerProvider(String serviceName) {
        Resource inner = Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)
        );
        Resource resource = Resource.getDefault().merge(inner);

        SpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .build();

        SpanProcessor spanProcessor = SpanProcessor.composite(
            BatchSpanProcessor.builder(spanExporter).build(),
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

    private static StringMapPropagator propagateContext(OpenTelemetrySdk openTelemetry) {
        StringMapPropagator mapContext = new StringMapPropagator();
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
        textMapPropagator.inject(Context.current(), mapContext, StringMapPropagator.setter());
        return mapContext;
    }
}
