package eu.malycha.zipkin.poc.otel;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
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

public class OpenTelemetryContext implements Closeable {

    static final String VERSION = "1.0.0";

    private final SdkTracerProvider tracerProvider;
    private final OpenTelemetrySdk openTelemetry;

    private OpenTelemetryContext(String serviceName) {
        this.tracerProvider = createTracerProvider(serviceName);
        this.openTelemetry = createOpenTelemetry(tracerProvider);
    }

    public static OpenTelemetryContext create(String serviceName) {
        return new OpenTelemetryContext(serviceName);
    }

    @Override
    @SuppressWarnings("EmptyTryBlock")
    public void close() throws IOException {
        try (Closeable tp = tracerProvider;
             Closeable ot = openTelemetry;) {
            // Empty
        }
    }

    public StringMapContext runInSpan(String scopeName, String spanName, Runnable runnable) {
        return runInSpan(scopeName, spanName, null, runnable);
    }

    public StringMapContext runInRootSpan(String scopeName, String spanName, StringMapContext contextMap, Runnable runnable) {
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
        Context context = textMapPropagator.extract(Context.current(), contextMap, StringMapContext.getter());
        return runInSpan(scopeName, spanName, context, runnable);
    }

    public StringMapContext runInSpan(String scopeName, String spanName, Context context, Runnable runnable) {
        Tracer tracer = openTelemetry.getTracer(scopeName, VERSION);
        Span span = tracer.spanBuilder(spanName).setParent(context).startSpan();
        try (Scope ss = span.makeCurrent()){
            runnable.run();
            return propagateContext(openTelemetry);
        } finally {
            span.end();
        }
    }

    private static SdkTracerProvider createTracerProvider(String serviceName) {
        Resource inner = Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)
        );
        Resource resource = Resource.getDefault().merge(inner);

        SpanExporter spanExporter = JaegerThriftSpanExporter.builder()
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

    private static StringMapContext propagateContext(OpenTelemetrySdk openTelemetry) {
        StringMapContext mapContext = new StringMapContext();
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
        textMapPropagator.inject(Context.current(), mapContext, StringMapContext.setter());
        return mapContext;
    }
}
