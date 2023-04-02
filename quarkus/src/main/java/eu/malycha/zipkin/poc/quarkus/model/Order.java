package eu.malycha.zipkin.poc.quarkus.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Order {

    public String orderId;

    public Order() {}

    public Order(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "Order{" +
            "orderId='" + orderId + '\'' +
            '}';
    }
}
