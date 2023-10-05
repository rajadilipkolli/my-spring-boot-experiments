package com.example.hibernatecache.model.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long customerId,
        Long orderId,
        String name,
        BigDecimal price,
        List<OrderItemResponse> orderItems) {}
