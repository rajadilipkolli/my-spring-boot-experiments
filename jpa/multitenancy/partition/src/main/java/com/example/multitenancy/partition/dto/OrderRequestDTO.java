package com.example.multitenancy.partition.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderRequestDTO(
        @NotNull(message = "Amount cannot be null")
                @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be positive")
                BigDecimal amount,
        @NotNull(message = "Order date cannot be null") LocalDate orderDate) {}
