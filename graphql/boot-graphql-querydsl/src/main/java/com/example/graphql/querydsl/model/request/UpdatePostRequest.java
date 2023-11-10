package com.example.graphql.querydsl.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UpdatePostRequest(
        @NotEmpty(message = "Title cannot be empty") String title,
        @NotBlank(message = "Content cannot be blank") String content) {}
