package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderRequest(
        Long customerId,
        @NotBlank(message = "Name cannot be blank") String name,
        @NotNull(message = "Price cannot be null") @DecimalMin(value = "0.01", message = "Value must be greater than or equal to 0.01")
                @DecimalMax(value = "10000.00", message = "Value must be less than or equal to 10000.00")
                @Digits(
                        integer = 3,
                        fraction = 2,
                        message = "Value must have at most 3 digits in integer part and 2 in fraction part")
                BigDecimal price) {}
