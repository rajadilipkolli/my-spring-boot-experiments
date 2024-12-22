package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        @NotEmpty(message = "Text cannot be empty") String text,
        @NotBlank(message = "CustomerId cannot be blank") String customerId) {}
