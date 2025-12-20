package com.example.jndi.model.request;

import jakarta.validation.constraints.NotEmpty;

public record DriverRequest(
        @NotEmpty(message = "Text cannot be empty") String text) {}
