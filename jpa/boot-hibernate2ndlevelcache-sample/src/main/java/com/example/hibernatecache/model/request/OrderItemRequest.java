package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotBlank(message = "Price cannot be blank") String price,
        @Positive(message = "Quantity must be positive") Integer quantity,
        Long orderId) {}
