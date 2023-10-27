package com.example.keysetpagination.model.request;

import jakarta.validation.constraints.NotEmpty;

public record ActorRequest(@NotEmpty(message = "Text cannot be empty") String text) {}
