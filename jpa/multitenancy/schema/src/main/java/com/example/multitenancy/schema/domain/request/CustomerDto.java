package com.example.multitenancy.schema.domain.request;

import jakarta.validation.constraints.NotBlank;

public record CustomerDto(
        @NotBlank(message = "Name cannot be Blank") String name) {}
