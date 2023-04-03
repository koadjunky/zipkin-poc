package eu.malycha.zipkin.poc.quarkus.infra;

import eu.malycha.zipkin.poc.quarkus.infra.otl.OpenTelemetryTracing;

public class TelemetryUtil {

    private TelemetryUtil() {
        // Empty
    }

    public static Tracing createTracing(String serviceName, String defaultScope, String defaultVersion) {
        return OpenTelemetryTracing.create(serviceName, defaultScope, defaultVersion);
    }
}
