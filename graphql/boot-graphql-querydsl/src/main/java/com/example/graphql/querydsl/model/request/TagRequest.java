package com.example.graphql.querydsl.model.request;

import jakarta.validation.constraints.NotEmpty;

public record TagRequest(
        @NotEmpty(message = "TagName cannot be empty") String name) {}
