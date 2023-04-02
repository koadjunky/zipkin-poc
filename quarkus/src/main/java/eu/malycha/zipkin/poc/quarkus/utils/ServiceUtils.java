package eu.malycha.zipkin.poc.quarkus.utils;

import java.time.Duration;

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
}
