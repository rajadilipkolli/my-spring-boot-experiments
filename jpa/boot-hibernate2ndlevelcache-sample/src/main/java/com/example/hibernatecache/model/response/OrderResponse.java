package com.example.hibernatecache.model.response;

import java.util.List;

public record OrderResponse(
        Long customerId, Long orderId, String text, List<OrderItemResponse> orderItems) {}
