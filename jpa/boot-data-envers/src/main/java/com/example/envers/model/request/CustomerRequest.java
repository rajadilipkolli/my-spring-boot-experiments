package com.example.envers.model.request;

import jakarta.validation.constraints.NotEmpty;

public record CustomerRequest(
        @NotEmpty(message = "Name cannot be empty") String name, String address) {}
