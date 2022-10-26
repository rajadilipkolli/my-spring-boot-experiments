package com.example.multitenancy.schema.domain.request;

import jakarta.validation.constraints.NotEmpty;

public record CustomerDto(@NotEmpty(message = "Name cannot be empty") String name) {}
