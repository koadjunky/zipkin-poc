package eu.malycha.zipkin.poc.otel;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringMapPropagator extends HashMap<String, String> {

    public static Getter getter() {
        return new Getter();
    }

    public static Setter setter() {
        return new Setter();
    }

    public static class Getter implements TextMapGetter<StringMapPropagator> {

        @Override
        public Iterable<String> keys(@Nonnull StringMapPropagator carrier) {
            return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(@Nullable StringMapPropagator carrier, @Nonnull String key) {
            return Optional.ofNullable(carrier)
                .map(c -> c.get(key))
                .orElse(null);
        }
    }

    public static class Setter implements TextMapSetter<StringMapPropagator> {

        @Override
        public void set(@Nullable StringMapPropagator carrier, @Nonnull String key, @Nonnull String value) {
            if (carrier != null) {
                carrier.put(key, value);
            }
        }
    }
}
