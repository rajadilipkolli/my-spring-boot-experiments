package com.example.graphql.querydsl.model.request;

import jakarta.validation.constraints.NotEmpty;

public record TagRequest(@NotEmpty(message = "Text cannot be empty") String text) {}
