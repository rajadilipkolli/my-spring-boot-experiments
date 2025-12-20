package com.example.cache.model.request;

import jakarta.validation.constraints.NotBlank;

public record MovieRequest(
        @NotBlank(message = "Title cannot be blank") String title) {}
