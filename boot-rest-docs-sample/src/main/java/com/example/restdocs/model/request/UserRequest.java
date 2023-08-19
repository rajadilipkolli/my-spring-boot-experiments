package com.example.restdocs.model.request;

import com.example.restdocs.entities.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UserRequest(
        @NotBlank(message = "FirstName can't be blank") String firstName,
        String lastName,
        @Positive(message = "Age must be greater than 0") Integer age,
        Gender gender,
        String phoneNumber) {}
