package eu.malycha.zipkin.poc.quarkus.infra.otl;

import eu.malycha.zipkin.poc.quarkus.infra.SafeCloseable;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanWrapper implements SafeCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpanWrapper.class);

    private Span span;
    private Scope scope;

    public SpanWrapper(Span span) {
        this.span = span;
        this.scope = span.makeCurrent();
    }

    @Override
    public void close() {
        closeScope();
        closeSpan();
    }

    private void closeSpan() {
        try {
            if (span != null) {
                span.end();
                span = null;
            }
        } catch (Exception ex) {
            // Don't want any problems to propagate outside of observability module
            LOGGER.error("Exception:", ex);
        }
    }

    private void closeScope() {
        try {
            if (scope != null) {
                scope.close();
                scope = null;
            }
        } catch (Exception ex) {
            // Don't want any problems to propagate outside of observability module
            LOGGER.error("Exception:", ex);
        }
    }
}
