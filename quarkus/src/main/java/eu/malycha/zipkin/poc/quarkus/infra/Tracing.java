package eu.malycha.zipkin.poc.quarkus.infra;

import java.io.Closeable;

public interface Tracing extends Closeable {

    SafeCloseable createSpan(String spanName);

    SafeCloseable createSpan(String scopeName, String version, String spanName);

}
