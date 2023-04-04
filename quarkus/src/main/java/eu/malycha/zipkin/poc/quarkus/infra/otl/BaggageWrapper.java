package eu.malycha.zipkin.poc.quarkus.infra.otl;

import eu.malycha.zipkin.poc.quarkus.infra.SafeCloseable;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaggageWrapper implements SafeCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaggageWrapper.class);

    private Scope scope;

    public BaggageWrapper(Baggage baggage) {
        this.scope = baggage.storeInContext(Context.current()).makeCurrent();
    }

    @Override
    public void close() {
        closeScope();
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
