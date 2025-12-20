package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "FirstName must not be blank") String firstName,
        String lastName,

        @Email(message = "Email value must be a well-formed email address") @NotBlank(message = "Email must not be blank") String email,

        String phone) {}
