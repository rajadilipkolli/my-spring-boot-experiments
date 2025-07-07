package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank(message = "Text cannot be empty") String text,
        @NotBlank(message = "CustomerId cannot be blank", groups = ValidationGroups.GroupCheck.class) String customerId) {}
