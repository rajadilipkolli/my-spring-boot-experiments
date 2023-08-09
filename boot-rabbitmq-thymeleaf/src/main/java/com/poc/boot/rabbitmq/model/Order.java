package com.poc.boot.rabbitmq.model;

import java.io.Serializable;

public record Order(String orderNumber, String productId, Double amount) implements Serializable {}
