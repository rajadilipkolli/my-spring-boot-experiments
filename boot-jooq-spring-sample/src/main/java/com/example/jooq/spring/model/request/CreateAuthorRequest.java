package com.example.jooq.spring.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(
        @NotBlank(message = "Name is required") String name,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {}
