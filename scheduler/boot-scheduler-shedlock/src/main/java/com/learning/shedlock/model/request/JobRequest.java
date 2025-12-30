package com.learning.shedlock.model.request;

import jakarta.validation.constraints.NotEmpty;

public record JobRequest(
        @NotEmpty(message = "Text cannot be empty") String text) {}
