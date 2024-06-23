package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.NotEmpty;

public record OrderItemRequest(@NotEmpty(message = "Text cannot be empty") String text, Long orderId) {}
