package eu.malycha.zipkin.poc.quarkus.infra;

public interface SafeCloseable extends AutoCloseable {

    @Override
    void close();
}
