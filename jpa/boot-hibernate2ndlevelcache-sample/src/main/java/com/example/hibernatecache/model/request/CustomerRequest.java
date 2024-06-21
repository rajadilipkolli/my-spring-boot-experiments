package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "FirstName cannot be blank") String firstName,
        String lastName,
        @Email String email,
        String phone) {}
