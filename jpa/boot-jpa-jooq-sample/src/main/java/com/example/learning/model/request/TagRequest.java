package com.example.learning.model.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(@NotBlank(message = "Name of tag is mandatory") String name, String description) {}
