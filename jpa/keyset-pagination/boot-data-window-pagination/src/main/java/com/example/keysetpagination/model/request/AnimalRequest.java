package com.example.keysetpagination.model.request;

import jakarta.validation.constraints.NotBlank;

public record AnimalRequest(@NotBlank(message = "Name cannot be blank") String name) {}
