package com.example.hibernatecache.model.request;

import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        Long customerId, @NotEmpty(message = "Text cannot be empty") String text) {}
