package eu.malycha.zipkin.poc.quarkus.utils;

import eu.malycha.zipkin.poc.quarkus.infra.Tracing;
import eu.malycha.zipkin.poc.quarkus.infra.otl.RabbitHeadersPropagator;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.time.Duration;
import java.util.Map;

public class ServiceUtils {

    private ServiceUtils() {
        // Empty
    }

    public static void delay(Duration duration) {
        delay(duration.toMillis());
    }

    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static <T> void emit(Emitter<T> emitter, T payload, Tracing tracing) {
        Map<String, Object> headers = tracing.saveContext(RabbitHeadersPropagator.create(), RabbitHeadersPropagator.setter()).getHeaders();
        OutgoingRabbitMQMetadata metadata = OutgoingRabbitMQMetadata.builder()
            .withHeaders(headers)
            .build();
        emitter.send(Message.of(payload, Metadata.of(metadata)));
    }
}
