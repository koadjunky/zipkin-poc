package eu.malycha.zipkin.poc.quarkus.infra.otl;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RabbitHeadersPropagator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitHeadersPropagator.class);

    private final Map<String, String> contextMap = new HashMap<>();

    private RabbitHeadersPropagator(Map<String, Object> headers) {
        headers.forEach((k, v) -> contextMap.put(k, v.toString()));
    }

    public static RabbitHeadersPropagator create(Map<String, Object> headers) {
        return new RabbitHeadersPropagator(headers);
    }

    public static RabbitHeadersPropagator create() {
        return new RabbitHeadersPropagator(Map.of());
    }

    public Map<String, Object> getHeaders() {
        return Collections.unmodifiableMap(contextMap);
    }

    public static TextMapGetter<RabbitHeadersPropagator> getter() {
        return new Getter();
    }

    public static TextMapSetter<RabbitHeadersPropagator> setter() {
        return new Setter();
    }

    private static class Getter implements TextMapGetter<RabbitHeadersPropagator> {

        @Override
        public Iterable<String> keys(RabbitHeadersPropagator carrier) {
            return carrier.contextMap.keySet();
        }

        @Nullable
        @Override
        public String get(@Nullable RabbitHeadersPropagator carrier, String key) {
            if (carrier == null) {
                return null;
            }
            String result = carrier.contextMap.get(key);
            LOGGER.info("Retrieving header '{}': '{}'", key, result);
            return  result;
        }
    }

    private static class Setter implements TextMapSetter<RabbitHeadersPropagator> {

        @Override
        public void set(@Nullable RabbitHeadersPropagator carrier, String key, String value) {
            if (carrier != null) {
                LOGGER.info("Setting header '{}': '{}'", key, value);
                carrier.contextMap.put(key, value);
            }
        }
    }
}
