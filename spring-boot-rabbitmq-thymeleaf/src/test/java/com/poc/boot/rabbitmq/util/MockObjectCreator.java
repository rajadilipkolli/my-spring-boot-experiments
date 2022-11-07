package com.poc.boot.rabbitmq.util;

import com.poc.boot.rabbitmq.model.Order;

public final class MockObjectCreator {

    private MockObjectCreator() {
        throw new UnsupportedOperationException("Cannot create object");
    }

    public static Order getOrder() {
        return new Order("1", "P1", 10D);
    }
}
