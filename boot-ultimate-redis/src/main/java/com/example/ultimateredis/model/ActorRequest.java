package com.example.ultimateredis.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ActorRequest(
        @NotBlank(message = "Name cannot be blank") String name,
        @Min(value = 0, message = "Age must be a positive number") Integer age) {}
