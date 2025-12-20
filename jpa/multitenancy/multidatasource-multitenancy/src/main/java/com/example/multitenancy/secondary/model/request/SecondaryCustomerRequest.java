package com.example.multitenancy.secondary.model.request;

import jakarta.validation.constraints.NotBlank;

public record SecondaryCustomerRequest(
        @NotBlank(message = "Name cannot be blank") String name) {}
