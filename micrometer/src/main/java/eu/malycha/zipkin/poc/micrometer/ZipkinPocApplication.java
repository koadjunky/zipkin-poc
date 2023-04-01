package eu.malycha.zipkin.poc.micrometer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZipkinPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinPocApplication.class, args);
    }
}
