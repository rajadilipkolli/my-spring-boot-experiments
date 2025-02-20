package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest(
        Long customerId,
        @NotBlank(message = "Name cannot be blank") String name,
        @Size(min = 1, message = "OrderItems are required") List<OrderItemRequest> orderItems) {}
