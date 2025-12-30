package com.example.archunit.model.request;

import jakarta.validation.constraints.NotEmpty;

public record ClientRequest(
        @NotEmpty(message = "Text cannot be empty") String text) {}
