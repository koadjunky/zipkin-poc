package eu.malycha.zipkin.poc.quarkus.infra;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringMapContext extends HashMap<String, String> {

    public static Getter getter() {
        return new Getter();
    }

    public static Setter setter() {
        return new Setter();
    }

    public static class Getter implements TextMapGetter<StringMapContext> {

        @Override
        public Iterable<String> keys(@Nonnull StringMapContext carrier) {
            return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(@Nullable StringMapContext carrier, @Nonnull String key) {
            return Optional.ofNullable(carrier)
                .map(c -> c.get(key))
                .orElse(null);
        }
    }

    public static class Setter implements TextMapSetter<StringMapContext> {

        @Override
        public void set(@Nullable StringMapContext carrier, @Nonnull String key, @Nonnull String value) {
            if (carrier != null) {
                carrier.put(key, value);
            }
        }
    }
}
