package eu.malycha.zipkin.poc.quarkus.infra;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.io.Closeable;
import java.util.Map;

public interface Tracing extends Closeable {

    SafeCloseable createSpan(String spanName);

    SafeCloseable createSpan(String spanName, Context parent); // TODO: Context wrapper

    SafeCloseable createSpan(String scopeName, String version, String spanName);

    SafeCloseable createSpan(String scopeName, String version, String spanName, Context parent);

    SafeCloseable createBaggage(Map<String, String> content);

    SafeCloseable createBaggage(Context context); // TODO: Context wrapper

    <C> Context loadContext(C propagator, TextMapGetter<C> getter);

    <C> C saveContext(C propagator, TextMapSetter<C> setter);

}
