package com.example.multitenancy.primary.model.request;

import jakarta.validation.constraints.NotBlank;

public record PrimaryCustomerRequest(
        @NotBlank(message = "Text cannot be blank") String text) {}
