package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero") @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimals") BigDecimal price,
        @Positive(message = "Quantity must be positive") Integer quantity,
        @NotBlank(message = "ItemCode Cant be Blank") String itemCode) {}
