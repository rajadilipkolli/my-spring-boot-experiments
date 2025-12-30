package com.example.keysetpagination.model.request;

import jakarta.validation.constraints.NotBlank;

public record ActorRequest(
        @NotBlank(message = "Name cannot be blank") String name) {}
