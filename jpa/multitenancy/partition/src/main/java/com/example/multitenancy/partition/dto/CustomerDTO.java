package com.example.multitenancy.partition.dto;

import jakarta.validation.constraints.NotEmpty;

public record CustomerDTO(
        @NotEmpty(message = "Text cannot be empty") String text) {}
